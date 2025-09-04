package com.kuklin.telegram_service.telegram.handlers.interview;

import com.kuklin.telegram_service.entities.TelegramUser;
import com.kuklin.telegram_service.integrations.HhFeignClient;
import com.kuklin.telegram_service.services.TelegramService;
import com.kuklin.telegram_service.sharedlibrary.HhResponseDto;
import com.kuklin.telegram_service.telegram.handlers.UpdateHandler;
import com.kuklin.telegram_service.telegram.utils.Command;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class UrlInterviewUpdateHandler implements UpdateHandler {
    private final HhFeignClient hhFeignClient;
    private final TelegramService telegramService;
    private final StartInterviewUpdateHandler startInterviewUpdateHandler;
    private static final String AI_REQUEST_MESSAGE =
            """
                            Я отправлю тебе описании вакансии.\s
                            Твоя задача - провести собеседовании для данной вакансии. Ты профессиональный ревьюер в области этой вакансии.
                            Я ожидаю, что ты будешь задавать вопросы, относящиеся к профессиональной деятельности данной вакансии.\s
                            Ты задаешь пользователю вопрос. Ждешь ответ и исправляешь ошибки в ответе пользователя. И переходишь к следующему вопросу.
                            Описание вакансии: %s
                    """;

    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String messageText = message.getText();

        HhResponseDto hhResponseDto = hhFeignClient.getVacancyById(extractVacancyId(messageText));
        String aiRequestMessage = String.format(AI_REQUEST_MESSAGE, hhResponseDto.getMainInfo());

        startInterviewUpdateHandler.processInterviewFlow(aiRequestMessage, chatId, telegramUser);

    }

    private boolean isValidatedUrl(String url) {
        return url.startsWith("https://hh.ru/vacancy") ||
                url.startsWith("hh.ru/vacancy")
                ;
    }

    private Long extractVacancyId(String url) {
        return Long.valueOf(url.substring(
                url.lastIndexOf("/") + 1, url.indexOf("?")));
    }

    @Override
    public String getHandlerListName() {
        return Command.URL_PROCESS.getCommandText();
    }
}
