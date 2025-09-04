package com.kuklin.telegram_service.telegram.handlers;

import com.kuklin.telegram_service.entities.TelegramUser;
import com.kuklin.telegram_service.integrations.UserServiceFeignClient;
import com.kuklin.telegram_service.services.TelegramService;
import com.kuklin.telegram_service.sharedlibrary.UserDto;
import com.kuklin.telegram_service.telegram.utils.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class IfUpdateHandler implements UpdateHandler {
    private final UserServiceFeignClient userServiceFC;
    private final TelegramService telegramService;
    private static final String ERROR_MESSAGE = "Произошла ошибка! Не получилось задать новые условия!";
    private static final String SUCCESS_MESSAGE = "Новые условия сохранены: ";
    @Override
    public void handle(Update update, TelegramUser telegramUser) {

        Message requestMessage = update.getMessage();
        Long chatId = requestMessage.getChatId();

        String properties = requestMessage.getText().substring(
                getHandlerListName().length());

        UserDto userDto = userServiceFC.setProperties(telegramUser.getUserId(), properties);

        telegramService.sendReturnedMessage(
                chatId, SUCCESS_MESSAGE + userDto.getProperties());
    }

    @Override
    public String getHandlerListName() {
        return Command.IF.getCommandText();
    }
}
