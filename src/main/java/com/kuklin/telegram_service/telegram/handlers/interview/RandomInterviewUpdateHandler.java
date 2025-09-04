package com.kuklin.telegram_service.telegram.handlers.interview;

import com.kuklin.telegram_service.entities.TelegramUser;
import com.kuklin.telegram_service.integrations.UserServiceFeignClient;
import com.kuklin.telegram_service.sharedlibrary.UserDto;
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
public class RandomInterviewUpdateHandler implements UpdateHandler {

    private final StartInterviewUpdateHandler startInterviewUpdateHandler;
    private final UserServiceFeignClient userServiceFC;
    private static final String AI_REQUEST_MESSAGE =
            "Ты проводишь собеседование на должность: %s\n" +
            "Начинай сразу с приветствия и первого вопроса. " +
            "Задавай короткие случайные вопросы, на которые можно быстро ответить! " +
            "Вопросы задавай по одному, после ответа поправь ошибки и переходи к следующему вопросу." +
            "\nНе обсуждай с пользователем сторонние темы. Не дай ему отойти от темы собеседования. " +
                    "Ты должен задать много коротких вопросов на различные темы, по одному за раз.";

    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        Message message = update.getMessage();
        Long chatId = message.getChatId();

        UserDto userDto = userServiceFC.getUserById(telegramUser.getUserId());

        String aiRequestMessage = String.format(AI_REQUEST_MESSAGE, userDto.getJobTitle());
        startInterviewUpdateHandler.processInterviewFlow(aiRequestMessage, chatId, telegramUser);
    }

    @Override
    public String getHandlerListName() {
        return Command.RANDOM_INTERVIEW.getCommandText();
    }
}
