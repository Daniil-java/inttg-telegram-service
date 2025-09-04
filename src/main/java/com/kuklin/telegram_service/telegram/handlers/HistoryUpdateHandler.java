package com.kuklin.telegram_service.telegram.handlers;

import com.kuklin.telegram_service.entities.TelegramUser;
import com.kuklin.telegram_service.integrations.InterviewFeignClient;
import com.kuklin.telegram_service.services.TelegramService;
import com.kuklin.telegram_service.sharedlibrary.InterviewDto;
import com.kuklin.telegram_service.telegram.utils.Command;
import com.kuklin.telegram_service.telegram.utils.ThreadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HistoryUpdateHandler implements UpdateHandler {
    private final InterviewFeignClient interviewFC;
    private final TelegramService telegramService;
    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        Message requestMessage = update.getMessage();
        Long chatId = requestMessage.getChatId();

        List<InterviewDto> interviewList = interviewFC.getLatestResultList(telegramUser.getUserId());
        for (InterviewDto i: interviewList) {
            telegramService.sendReturnedMessage(chatId, i.getResult());
            ThreadUtil.sleep(100);
        }
    }

    @Override
    public String getHandlerListName() {
        return Command.HISTORY.getCommandText();
    }
}
