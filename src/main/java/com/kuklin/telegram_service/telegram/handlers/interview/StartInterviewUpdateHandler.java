package com.kuklin.telegram_service.telegram.handlers.interview;

import com.kuklin.telegram_service.entities.TelegramUser;
import com.kuklin.telegram_service.integrations.AiConversationFeignClient;
import com.kuklin.telegram_service.integrations.InterviewFeignClient;
import com.kuklin.telegram_service.integrations.UserServiceFeignClient;
import com.kuklin.telegram_service.services.TelegramService;
import com.kuklin.telegram_service.services.TelegramUserService;
import com.kuklin.telegram_service.sharedlibrary.*;
import com.kuklin.telegram_service.telegram.handlers.UpdateHandler;
import com.kuklin.telegram_service.telegram.utils.BotState;
import com.kuklin.telegram_service.telegram.utils.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class StartInterviewUpdateHandler implements UpdateHandler {
    private final TelegramService telegramService;
    private final TelegramUserService telegramUserService;
    private final InterviewFeignClient interviewFC;
    private final AiConversationFeignClient aiConversationFC;
    private final UserServiceFeignClient userServiceFC;
    private static final String JOB_ERROR_MESSAGE = "Произошла ошибка! Вы должны указать должность при помощи команды: /job";
    private static final String ERROR_MESSAGE = "Произошла ошибка! Попробуйте обратиться позже!";

    private static final String AI_REQUEST_MESSAGE = "Ты проводишь собеседование на должность: %s\n" +
            "Начинай сразу с приветствия и первого вопроса. " +
            "Вопросы задавай по одному, после ответа переходи к следующему вопросу." +
            "\nНе обсуждай с пользователем сторонние темы. Не дай ему отойти от темы собеседования." +
            "Ты не ИИ, ты профессиональый ревьюер с опытом в соответствующей сфере ." +
            "Дополнительные условия следующие: %s";

    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        Message requestMessage = update.getMessage();
        Long chatId = requestMessage.getChatId();

        UserDto userDto = userServiceFC.getUserById(telegramUser.getUserId());

        if (userDto.getJobTitle() == null || userDto.getJobTitle().isEmpty()) {
            telegramService.sendReturnedMessage(chatId, JOB_ERROR_MESSAGE);
            return;
        }

        String aiRequestMessage = String.format(AI_REQUEST_MESSAGE,
                userDto.getJobTitle(), userDto.getProperties());

        processInterviewFlow(aiRequestMessage, chatId, telegramUser);

    }

    public void processInterviewFlow(String aiRequestMessage, Long chatId, TelegramUser telegramUser) {
        Long conversationId = aiConversationFC
                .postNewConversationDto(new ConversationDto()
                        .setUserId(telegramUser.getUserId()))
                .getId();

        String response;
        try {
            MessageResponseDto message = aiConversationFC.sendUserMessage(
                    MessageRequestDto.getDefault(aiRequestMessage, conversationId)
                            .setUserId(telegramUser.getUserId()));
            response = message.getContent();

            telegramUserService.setActualConversationIdOrGetNull(
                    telegramUser.getTelegramId(), conversationId);

            telegramUserService.save(telegramUser
                    .setBotState(BotState.INTERVIEW)
                    .setActualAiConversationId(conversationId)
            );

            interviewFC.createInterview(new InterviewRequest()
                    .setUserId(telegramUser.getUserId())
                    .setConversationId(conversationId)
            );
        } catch (Exception e) {
            response = ERROR_MESSAGE;
            telegramUser.setBotState(BotState.WAIT);
        }
        telegramService.sendReturnedMessage(chatId, response);
    }

    @Override
    public String getHandlerListName() {
        return Command.START_INTERVIEW.getCommandText();
    }
}
