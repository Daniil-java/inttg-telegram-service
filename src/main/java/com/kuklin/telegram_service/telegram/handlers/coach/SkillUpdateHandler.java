package com.kuklin.telegram_service.telegram.handlers.coach;

import com.kuklin.telegram_service.entities.TelegramUser;
import com.kuklin.telegram_service.integrations.InterviewFeignClient;
import com.kuklin.telegram_service.services.TelegramService;
import com.kuklin.telegram_service.sharedlibrary.SkillDto;
import com.kuklin.telegram_service.sharedlibrary.VacancyDto;
import com.kuklin.telegram_service.telegram.handlers.UpdateHandler;
import com.kuklin.telegram_service.telegram.utils.Command;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SkillUpdateHandler implements UpdateHandler {
    private final TelegramService telegramService;
    private final InterviewFeignClient interviewFC;
    private final TopicUpdateHandler topicUpdateHandler;

    public static final String ID_LIST_COMMAND = "/skillId#";
    private static final String NEXT_COMMAND = "/prevPage#";
    private static final String PREV_COMMAND = "/nextPage#";
    public static final String VACANCY_ID = "/vacancyId";
    private static final int LIST_PAGE_ROW_COUNT = 5;

    @Override
    public void handle(Update update, TelegramUser telegramUser) {
        if (update.hasCallbackQuery()) {
            processCallback(update.getCallbackQuery());
        }
    }

    //Обработка callback
    //Отправляет пользователю клавиатуру-список скиллов
    private void processCallback(CallbackQuery callbackQuery) {
        //Данные, хранящиеся в callback
        String data = callbackQuery.getData();
        //Извлечение номера страницы, из данных
        int page = extractPage(data);
        //Извлечение идентификатора вакансии, из данных
        long vacancyId = extractVacancyId(data);
        sendSkillsPage(callbackQuery, vacancyId, page);
    }

    /*
    Формирует текст с информацией о вакансии и навыках для указанной страницы,
    и отправляет его в Telegram, заменяя текущее сообщение
     */
    private void sendSkillsPage(CallbackQuery callbackQuery, long vacancyId, int page) {
        //Получение вакансии, по ранее извлеченному идентификатору вакансии
        VacancyDto vacancy = interviewFC.getVacancyById(vacancyId);

        //Получение списка скиллов с пагинацией по идентификатору
        List<SkillDto> skillList = interviewFC.getPagingSkillsByVacancyId(
                vacancyId, page, LIST_PAGE_ROW_COUNT
        );

        //Формирование текстового содержания сообщения
        String message = getSkillsInfo(vacancy, skillList);

        //Редактирование существующего сообщения
        telegramService.sendEditMessage(
                callbackQuery.getMessage().getChatId(),
                message,
                callbackQuery.getMessage().getMessageId(),
                getInlineMessageSkillListButtons(skillList, vacancyId, LIST_PAGE_ROW_COUNT, page)
        );
    }

    //Формирование текстового содержания сообщения
    private String getSkillsInfo(VacancyDto vacancy, List<SkillDto> skillList) {
        return "Специальность: " + vacancy.getTitle() + "\n" +
                skillList.stream()
                        .map(s -> "[" + s.getCategory() + "]: " + s.getName())
                        .collect(Collectors.joining("\n"));
    }

    //Извлечение идентификатора вакансии из данных callback,
    //В СЛУЧАЕ - пролистывания списка пользователем
    private long extractVacancyId(String data) {
        String temp = data.substring(getHandlerListName().length() + 1);
        int start = VACANCY_ID.length();
        int slashIndex = temp.indexOf("/", start);
        if (slashIndex == -1) slashIndex = temp.length();
        return Long.parseLong(temp.substring(start, slashIndex));
    }

    //Извлечение номера страницы из данных callback,
    //В СЛУЧАЕ - пролистывания списка пользователем
    private int extractPage(String data) {
        if (data.contains(NEXT_COMMAND) || data.contains(PREV_COMMAND)) {
            return Integer.parseInt(
                    data.substring(
                            data.contains(NEXT_COMMAND)
                                    ? data.indexOf(NEXT_COMMAND) + NEXT_COMMAND.length()
                                    : data.indexOf(PREV_COMMAND) + PREV_COMMAND.length()
                    )
            );
        } else {
            return 0;
        }
    }

    @Override
    public String getHandlerListName() {
        return Command.SKILL.getCommandText();
    }

    //Создание клавиатуры-списка, с кнопками навигации
    private InlineKeyboardMarkup getInlineMessageSkillListButtons(
            List<SkillDto> skillList, long vacancyId, int rowCount, int page
    ) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        //Кнопки с навыками
        for (SkillDto skill : skillList) {
            InlineKeyboardButton skillButton = new InlineKeyboardButton(
                    String.format("[%s]: %s", skill.getId(), skill.getName())
            );
            skillButton.setCallbackData(
                    topicUpdateHandler.getHandlerListName()
                    + " " + ID_LIST_COMMAND + skill.getId()
            );
            rows.add(Collections.singletonList(skillButton));
        }

        //Навигация
        List<InlineKeyboardButton> navRow = new ArrayList<>();
        if (page > 0) {
            InlineKeyboardButton prevButton = new InlineKeyboardButton("⬅️");
            prevButton.setCallbackData(
                    getHandlerListName() + " " +
                            VACANCY_ID + vacancyId +
                            PREV_COMMAND + (page - 1)
            );
            navRow.add(prevButton);
        }
        if (skillList.size() == rowCount) {
            InlineKeyboardButton nextButton = new InlineKeyboardButton("➡️");
            nextButton.setCallbackData(
                    getHandlerListName() + " " +
                            VACANCY_ID + vacancyId +
                            NEXT_COMMAND + (page + 1)
            );
            navRow.add(nextButton);
        }
        if (!navRow.isEmpty()) {
            rows.add(navRow);
        }

        //Кнопка "BACK"
        InlineKeyboardButton backButton = new InlineKeyboardButton("BACK");
        backButton.setCallbackData(Command.COACH.getCommandText());
        rows.add(Collections.singletonList(backButton));

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
}
