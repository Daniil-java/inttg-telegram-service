package com.kuklin.telegram_service.telegram.handlers;

import com.kuklin.telegram_service.entities.TelegramUser;
import com.kuklin.telegram_service.integrations.AiConversationFeignClient;
import com.kuklin.telegram_service.integrations.UserServiceFeignClient;
import com.kuklin.telegram_service.services.TelegramService;
import com.kuklin.telegram_service.sharedlibrary.MessageRequestDto;
import com.kuklin.telegram_service.sharedlibrary.UserDto;
import com.kuklin.telegram_service.sharedlibrary.exceptions.ErrorResponseException;
import com.kuklin.telegram_service.telegram.utils.Command;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobUpdateHandler implements UpdateHandler {

    private final TelegramService telegramService;
    private final UserServiceFeignClient userServiceFC;
    private final AiConversationFeignClient aiConversationFC;
    private static final String JOB_ERROR_MESSAGE = "Произошла ошибка! Введите существующую должность.";

    private static final String ERROR_MESSAGE = "Произошла ошибка! Не получилось сохранить новую должность:";
    private static final String SUCCESS_MESSAGE = "Новая должность сохранена: ";

    private static final String AI_REQUEST_MESSAGE = "Ты должен ответить на вопрос " +
            "является ли следующая строка должна являться должностью, " +
            "на которую можно провести собеседование. Ответ должен быть однозначным," +
            " не пиши ничего лишнего, только 1 - если является, и 0 - если нет;\n" +
            "Строка: %s";

    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        Message requestMessage = update.getMessage();
        Long chatId = requestMessage.getChatId();
        String response;

        String jobTitle = requestMessage.getText().substring(
                getHandlerListName().length());

        try {
            MessageRequestDto messageRequestDto =
                    MessageRequestDto.getServiceMessage(
                            String.format(AI_REQUEST_MESSAGE, jobTitle));
            messageRequestDto.setUserId(telegramUser.getUserId());

            response = aiConversationFC.sendServiceMessage(messageRequestDto);
        } catch (ErrorResponseException e) {
            log.error("Ошибка при попытке отправить сервисное сообщение в ИИ-чат!", e);
            telegramService.sendReturnedMessage(chatId, e.getErrorStatus().getMessage());
            return;
        }

        if (response.equals("0")) {
            telegramService.sendReturnedMessage(chatId, JOB_ERROR_MESSAGE);
            return;
        }

        UserDto userDto = userServiceFC.setJobTitle(telegramUser.getUserId(), jobTitle);

        telegramService.sendReturnedMessage(
                chatId, SUCCESS_MESSAGE + userDto.getJobTitle());

    }

    @Override
    public String getHandlerListName() {
        return Command.JOB.getCommandText();
    }
}
