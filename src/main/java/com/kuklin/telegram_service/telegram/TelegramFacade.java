package com.kuklin.telegram_service.telegram;

import com.kuklin.telegram_service.entities.TelegramUser;
import com.kuklin.telegram_service.integrations.UserServiceFeignClient;
import com.kuklin.telegram_service.services.TelegramUserService;
import com.kuklin.telegram_service.telegram.handlers.UpdateHandler;
import com.kuklin.telegram_service.telegram.utils.BotState;
import com.kuklin.telegram_service.telegram.utils.Command;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class TelegramFacade {

    @Autowired
    private UserServiceFeignClient userServiceFC;
    @Autowired
    private TelegramUserService telegramUserService;
    private Map<String, UpdateHandler> updateHandlerMap = new ConcurrentHashMap<>();

    public void register(String command, UpdateHandler updateHandler) {
        if (updateHandlerMap.containsKey(command)) {
            log.error("This command is already exists!");
        }
        updateHandlerMap.put(command, updateHandler);
    }

    public void handleUpdate(Update update) {
        /*
         На этапе разработки обработка сообщений
         происходит только от одного пользователя
         */
        if (!update.hasCallbackQuery() && !update.hasMessage()) return;
        User user = update.getMessage() != null ?
                update.getMessage().getFrom() :
                update.getCallbackQuery().getFrom();
        if (!List.of(425120436L, 420478432L).contains(user.getId())) return;

        //Получаем или создаем акаунт в базе
        TelegramUser telegramUser = telegramUserService.createOrGetUserByTelegram(user);

        processInputUpdate(update, telegramUser).handle(update, telegramUser);
    }

    private UpdateHandler processInputUpdate(Update update, TelegramUser telegramUser) {

        String request;
        if (update.hasCallbackQuery()) {
            request = update.getCallbackQuery().getData();
        } else {
            request = update.getMessage().getText();
        }


        UpdateHandler currentUpdateHandler = null;
        if (request != null) {
            currentUpdateHandler = updateHandlerMap.get(request.split(TelegramBot.DELIMITER)[0]);
        }
        if (currentUpdateHandler != null) {
            return currentUpdateHandler;
        } else if (telegramUser.getBotState() == BotState.WAIT) {
            if (isValidatedUrl(request)) {
                return updateHandlerMap.get(Command.URL_PROCESS.getCommandText());
            } else {
                return updateHandlerMap.get(Command.ERROR.getCommandText());
            }
        } else if (telegramUser.getBotState() == BotState.TOPIC_INTERVIEW) {
            return updateHandlerMap.get(Command.TOPIC_INTERVIEW.getCommandText());
        } else {
            return updateHandlerMap.get(Command.INTERVIEW.getCommandText());
        }

    }

    private boolean isValidatedUrl(String url) {
        return url.startsWith("https://hh.ru/vacancy") ||
                url.startsWith("hh.ru/vacancy")
                ;
    }
}
