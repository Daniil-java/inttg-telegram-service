package com.kuklin.telegram_service.telegram.handlers.interview;

import com.kuklin.telegram_service.entities.TelegramUser;
import com.kuklin.telegram_service.integrations.AiConversationFeignClient;
import com.kuklin.telegram_service.integrations.InterviewFeignClient;
import com.kuklin.telegram_service.services.TelegramService;
import com.kuklin.telegram_service.services.TelegramUserService;
import com.kuklin.telegram_service.sharedlibrary.InterviewRequest;
import com.kuklin.telegram_service.sharedlibrary.MessageRequestDto;
import com.kuklin.telegram_service.sharedlibrary.MessageResponseDto;
import com.kuklin.telegram_service.telegram.handlers.UpdateHandler;
import com.kuklin.telegram_service.telegram.utils.BotState;
import com.kuklin.telegram_service.telegram.utils.Command;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j
public class EndInterviewUpdateHandler implements UpdateHandler {
    private final TelegramService telegramService;
    private final TelegramUserService telegramUserService;
    private final AiConversationFeignClient aiConversationFC;
    private final InterviewFeignClient interviewFC;
    private static final String ERROR_MESSAGE = "Произошла ошибка! Не получилось закончить собеседование!";
    private static final String AI_REQUEST_MESSAGE = "Собеседование закончено. " +
            "Проанализируй ответы. Расскажи об ошибках. " +
            "Сделай выводы. Скажи какие темы стоит подтянуть";
    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        Message requestMessage = update.getMessage();
        Long chatId = requestMessage.getChatId();

        telegramUser.setBotState(BotState.WAIT);

        Long conversationId = telegramUser.getActualAiConversationId();
        String response;
        try {
            MessageResponseDto message = aiConversationFC.sendUserMessage(
                    MessageRequestDto.getDefault(AI_REQUEST_MESSAGE, conversationId)
                            .setUserId(telegramUser.getUserId()));
            response = message.getContent();

            interviewFC.setInterviewResult(new InterviewRequest()
                    .setConversationId(conversationId)
                    .setResult(response)
                    .setUserId(telegramUser.getUserId()));

        } catch (Exception e) {
            log.error("Ошибка при попытке отправить сообщение в ИИ-чат!", e);
            response = ERROR_MESSAGE;
        }
        telegramUserService.save(telegramUser);
        telegramService.sendReturnedMessage(chatId, response);
    }

    @Override
    public String getHandlerListName() {
        return Command.END.getCommandText();
    }
}
