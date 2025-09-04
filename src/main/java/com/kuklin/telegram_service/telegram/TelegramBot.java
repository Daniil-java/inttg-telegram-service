package com.kuklin.telegram_service.telegram;

import com.kuklin.telegram_service.telegram.configurations.TelegramBotKeyComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    public static final String DELIMITER = " ";
    @Value("${bot.name}")
    private String botName;
    @Autowired
    private TelegramFacade telegramFacade;
    private String botToken;


    public TelegramBot(TelegramBotKeyComponent telegramBotKeyComponent) {
        super(telegramBotKeyComponent.getKey());
        botToken = telegramBotKeyComponent.getKey();
    }

    @Override
    public void onUpdateReceived(Update update) {
        telegramFacade.handleUpdate(update);
    }

    public void sendMessage(BotApiMethod sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Send message error!", e);
        }
    }

    public void sendVoiceMessage(SendVoice message) throws TelegramApiException{
        execute(message);
    }

    public Message sendReturnedMessage(SendMessage sendMessage) {
        try {
            return execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Send returned message error!", e);
        }
        return null;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    public String getToken() {
        return botToken;
    }
}
