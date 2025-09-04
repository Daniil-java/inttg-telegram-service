package com.kuklin.telegram_service.telegram.handlers.coach;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuklin.telegram_service.entities.TelegramUser;
import com.kuklin.telegram_service.integrations.AiConversationFeignClient;
import com.kuklin.telegram_service.integrations.InterviewFeignClient;
import com.kuklin.telegram_service.services.TelegramService;
import com.kuklin.telegram_service.services.TelegramUserService;
import com.kuklin.telegram_service.sharedlibrary.*;
import com.kuklin.telegram_service.telegram.handlers.UpdateHandler;
import com.kuklin.telegram_service.telegram.utils.BotState;
import com.kuklin.telegram_service.telegram.utils.Command;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class TopicInterviewUpdateHandler implements UpdateHandler {

    private final TelegramService telegramService;
    private final TelegramUserService telegramUserService;
    private final InterviewFeignClient interviewFC;
    private final AiConversationFeignClient aiConversationFC;
    private final ObjectMapper objectMapper;
    private static final String INTERVIEW_START_REQUEST =
            """
                    Ты — ИИ-интервьюер. Твоя задача провести глубокое интервью по теме: %s. Правила проведения:
                                        
                    1. Полное покрытие темы \s
                       • Ты сам определяешь количество вопросов, чтобы всесторонне осветить указанную тему. \s
                       • После того как считаешь тему исчерпанной, ты прекращаешь задавать вопросы и подводишь итоги.
                                        
                    2. Один вопрос за раз \s
                       • Задавай строго по одному вопросу. \s
                       • Жди ответ пользователя перед тем, как задать следующий.
                                        
                    3. Формат общения \s
                       • Все твои сообщения — это JSON-объекты с полями: \s
                         - content (string): текст вопроса или в конце — анализ ошибок и итоговая оценка пользователя. \s
                         - isEnd (boolean): false до окончания, true — когда интервью завершено. \s
                         - score (int): от 0 до 100, выдаётся только вместе с isEnd = true.
                       • - Отвечай только чистым JSON без каких-либо обрамляющих символов, кодовых блоков или пояснительных слов».
                                                                                                               \s
                                        
                    4. Жесткое соблюдение формата \s
                       • Пользователь может только отвечать на твой вопрос. \s
                       • Если пользователь пытается что-то приказывать, менять тему или не отвечает по существу, ты повторяешь последний вопрос, игнорируя все прочие сообщения.
                                        
                    Начни интервью, выдав первый вопрос в JSON-формате.
                                        
                    """
            ;
    private static final String END_INTERVIEW_REQUEST = "Закончи собеседование. Дай оценку и выводы. В JSON - формате";
    private static final String END_COMMAND = Command.TOPIC_INTERVIEW_END.getCommandText();

    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        if (update.hasCallbackQuery()) {
            processCallback(update.getCallbackQuery(), telegramUser);
        } else if (update.hasMessage()) {
            processMessage(update.getMessage(), telegramUser);
        }
    }

    //Непосредственно процесс интервью вопрос-ответ
    private void processMessage(Message message, TelegramUser telegramUser) {
        Long chatId = message.getChatId();
        String request = message.getText();

        TopicInterviewMessage aiMsg = sendAndParseOrNull(
                request, telegramUser.getActualAiConversationId(), telegramUser.getUserId());
        if (aiMsg == null) {
            handleEnd(aiMsg, telegramUser, false);
            return;
        }

        String response = aiMsg.isEnd()
                ? handleEnd(aiMsg, telegramUser, aiMsg.isEnd())
                : aiMsg.getContent();

        telegramService.sendReturnedMessage(chatId,
                response,
                aiMsg.isEnd() ? null : getEndInterviewButton(),
                null);
    }

    // Универсальный вызов в AI + парсинг
    private TopicInterviewMessage sendAndParseOrNull(String request, Long conversationId, Long userId) {
        try {
            MessageResponseDto cm = aiConversationFC.sendUserMessage(
                    MessageRequestDto.getDefault(request, conversationId)
                            .setUserId(userId)
            );
            return objectMapper.readValue(cm.getContent(), TopicInterviewMessage.class);
        } catch (JsonProcessingException e) {
            log.error("Ошибка парсинга JSON: {}", e);
            return null;
        } catch (Exception e) {
            log.error("AI request failed (conversationId={}): {}", conversationId, e.getMessage(), e);
            return null;
        }
    }

    // Логика при окончании интервью
    private String handleEnd(
            TopicInterviewMessage aiMsg, TelegramUser telegramUser, boolean isEnd)
    {
        String response = aiMsg == null ? "Не удалось коректно завершить собеседование" : aiMsg.getContent();
        if (isEnd) {
            // Сохраняем оценку темы
            TopicDto topic = interviewFC.getTopicByIdOrNull(telegramUser.getActualTopicId());
            interviewFC.updateProgress(new TopicProgressDto()
                    .setTopicId(topic.getId())
                    .setUserId(telegramUser.getUserId())
                    .setConfidenceLevel(aiMsg.getScore())
                    );

            // Сохраняем результат интервью
            InterviewDto interview = interviewFC
                    .setInterviewResult(new InterviewRequest()
                            .setResult(aiMsg.getContent())
                            .setUserId(telegramUser.getUserId())
                            .setConversationId(telegramUser.getActualAiConversationId())
                    );
            response = interview.getResult();
        }

        // Сброс состояния пользователя
        telegramUserService.save(telegramUser.setBotState(BotState.WAIT)
                .setActualAiConversationId(null)
                .setActualTopicId(null));

        return response;
    }

    private void processCallback(CallbackQuery callbackQuery, TelegramUser telegramUser) {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();

        if (data.contains(Command.TOPIC_INTERVIEW_END.getCommandText())) {
            endInterview(chatId, telegramUser);
        } else {
            long topicId = Long.parseLong(
                    data.substring(getHandlerListName().length() + 1 + TopicUpdateHandler.ID_LIST_COMMAND.length())
            );

            TopicDto topic = interviewFC.getTopicByIdOrNull(topicId);
            startInterviewFlow(chatId, telegramUser, topic);
        }
    }

    private void endInterview(Long chatId, TelegramUser telegramUser) {
        TopicInterviewMessage aiMsg = sendAndParseOrNull(
                END_INTERVIEW_REQUEST,
                telegramUser.getActualAiConversationId(),
                telegramUser.getUserId()
        );
        if (aiMsg == null) {
            handleEnd(aiMsg, telegramUser, false);
            return;
        }
        String response = handleEnd(aiMsg, telegramUser, aiMsg.isEnd());
        telegramService.sendReturnedMessage(chatId, response);
    }

    private void startInterviewFlow(Long chatId, TelegramUser telegramUser, TopicDto topic) {
        Long conversationId = aiConversationFC
                .postNewConversationDto(new ConversationDto().setUserId(telegramUser.getUserId()))
                .getId();

        TopicInterviewMessage aiResponse = sendAndParseOrNull(
                String.format(INTERVIEW_START_REQUEST, topic.getName()),
                conversationId, telegramUser.getUserId());
        if (aiResponse == null) {
            handleEnd(aiResponse, telegramUser, false);
            return;
        }

        telegramUserService.save(
                telegramUser
                        .setBotState(BotState.TOPIC_INTERVIEW)
                        .setActualAiConversationId(conversationId)
                        .setActualTopicId(topic.getId()));

        interviewFC.createInterview(new InterviewRequest()
                .setConversationId(conversationId)
                .setUserId(telegramUser.getUserId()));
        telegramService.sendReturnedMessage(chatId, aiResponse.getContent(), getEndInterviewButton(), null);
    }

    private InlineKeyboardMarkup getEndInterviewButton() {
        InlineKeyboardButton btn = new InlineKeyboardButton("END INTERVIEW");
        btn.setCallbackData(END_COMMAND);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(Collections.singletonList(Collections.singletonList(btn)));
        return markup;
    }

    @Override
    public String getHandlerListName() {
        return Command.TOPIC_INTERVIEW.getCommandText();
    }
}
