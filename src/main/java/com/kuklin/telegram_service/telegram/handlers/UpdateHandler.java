package com.kuklin.telegram_service.telegram.handlers;

import com.kuklin.telegram_service.entities.TelegramUser;
import com.kuklin.telegram_service.telegram.TelegramFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateHandler {
    void handle(Update update, TelegramUser telegramUser);

    String getHandlerListName();

    @Autowired
    default void registerMyself(TelegramFacade messageFacade) {
        messageFacade.register(getHandlerListName(), this);
    }

}
