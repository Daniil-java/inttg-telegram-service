package com.kuklin.telegram_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuklin.telegram_service.integrations.TelegramFeignClient;
import com.kuklin.telegram_service.telegram.TelegramBot;
import com.kuklin.telegram_service.telegram.utils.TelegramBotFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
public class TelegramService {

    private final TelegramBot telegramBot;
    private final TelegramFeignClient telegramFeignClient;

    public Message sendReturnedMessage(long chatId, String text,
                                       ReplyKeyboard replyKeyboard, Integer replyMessageId) {

        SendMessage sendMessage = buildMessage(chatId, text, replyKeyboard, replyMessageId);
        Message message = telegramBot.sendReturnedMessage(sendMessage);
        if (message == null) {
            sendMessage.setParseMode(null);
            telegramBot.sendReturnedMessage(sendMessage);
        }
        return message;
    }

    public Message sendReturnedMessage(long chatId, String text) {
        return sendReturnedMessage(chatId, text, null, null);
    }

    public void sendEditMessage(long chatId, String text,
                                   int messageId, InlineKeyboardMarkup inlineKeyboardMarkup) {

        telegramBot.sendMessage(buildEditMessage(chatId, text, messageId, inlineKeyboardMarkup));
    }

    public void sendVoiceMessage(long chatId, byte[] outputAudioFile, String filename) throws TelegramApiException {
        String format = ".mp3";
        if (!filename.endsWith(format)) {
            filename = filename.trim() + format;
        }
        SendVoice sendVoice = new SendVoice(
                String.valueOf(chatId),
                new InputFile(new ByteArrayInputStream(outputAudioFile),
                        filename));
        telegramBot.sendVoiceMessage(sendVoice);
    }

    private SendMessage buildMessage(long chatId, String text,
                                     ReplyKeyboard replyKeyboard, Integer replyMessageId) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(replyKeyboard)
                .replyToMessageId(replyMessageId)
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .build();
    }

    private EditMessageText buildEditMessage(long chatId, String text, int messageId,
                                             InlineKeyboardMarkup inlineKeyboardMarkup) {
        return EditMessageText.builder()
                .chatId(chatId)
                .text(text)
                .messageId(messageId)
                .replyMarkup(inlineKeyboardMarkup)
                .parseMode(ParseMode.HTML)
                .build();
    }

    public TelegramBotFile getFileOrNull(String fileId) {
        try {
            return new ObjectMapper().readValue(
                    telegramFeignClient.getFileRaw(
                            telegramBot.getToken(), fileId),
                    TelegramBotFile.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public byte[] downloadFile(TelegramBotFile telegramBotFile) {
        return telegramFeignClient.downloadFile(
                telegramBot.getToken(),
                telegramBotFile.getResult().getFilePath());
    }

    public byte[] downloadFileOrNull(String fileId) {
        TelegramBotFile telegramBotFile = getFileOrNull(fileId);
        if (telegramBotFile == null) return null;

        return downloadFile(telegramBotFile);
    }




}
