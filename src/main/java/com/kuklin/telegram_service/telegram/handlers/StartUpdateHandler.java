package com.kuklin.telegram_service.telegram.handlers;

import com.kuklin.telegram_service.entities.TelegramUser;
import com.kuklin.telegram_service.services.TelegramService;
import com.kuklin.telegram_service.telegram.utils.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class StartUpdateHandler implements UpdateHandler {

    private static final String START_MESSAGE =
            "Привет! Я ваш виртуальный помощник, готовый помочь вам с процессом " +
                    "трудоустройства. Давайте начнем!" +
                    "\nОписание комманд:" +
                    "\n- /job: Выберите желаемую должность для начала настроек интервью.\n" +
                    "- /if: Укажите условия, с которыми может работать ИИ, и которые могут быть изменены.\n" +
                    "- /start_interview: Запуск собеседования на выбранную должность. Должность должна быть предварительно задана.\n" +
                    "- /end: Завершение собеседования, подсчет результатов и получение анализа.\n" +
                    "- /history: Просмотрите записи прошлых собеседований.\n" +
                    "- /myResults: Получите общий анализ ваших сильных и слабых сторон.";
    private final TelegramService telegramService;

    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        telegramService.sendReturnedMessage(update.getMessage().getChatId(), START_MESSAGE);
    }

    @Override
    public String getHandlerListName() {
        return Command.START.getCommandText();
    }
}
