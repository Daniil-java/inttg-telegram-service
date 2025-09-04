package com.kuklin.telegram_service.telegram.handlers.progress;

import com.kuklin.telegram_service.entities.TelegramUser;
import com.kuklin.telegram_service.integrations.InterviewFeignClient;
import com.kuklin.telegram_service.services.TelegramService;
import com.kuklin.telegram_service.sharedlibrary.SkillDto;
import com.kuklin.telegram_service.sharedlibrary.TopicDto;
import com.kuklin.telegram_service.sharedlibrary.TopicProgressDto;
import com.kuklin.telegram_service.sharedlibrary.VacancyDto;
import com.kuklin.telegram_service.telegram.handlers.UpdateHandler;
import com.kuklin.telegram_service.telegram.utils.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProgressUpdateHandler implements UpdateHandler {
    private final TelegramService telegramService;
    private final InterviewFeignClient interviewFC;

    @Override
    @Transactional(readOnly = true)
    public void handle(Update update, TelegramUser telegramUser) {
        Message requestMessage = update.getMessage();
        Long chatId = requestMessage.getChatId();

        telegramService.sendReturnedMessage(chatId, generateReportForUser(telegramUser));
    }

    public String generateReportForUser(TelegramUser user) {
        // 1) Получаем все прогрессы по топикам для пользователя
        List<TopicProgressDto> progresses = interviewFC.getTopicProgressByUserId(user.getUserId());

        // собираем мапу: topicId → confidenceLevel
        Map<Long, Integer> progressByTopic = progresses.stream()
                .collect(Collectors.toMap(
                        TopicProgressDto::getTopicId,
                        TopicProgressDto::getConfidenceLevel,
                        (a, b) -> a
                ));

        // 2) Загружаем вакансии через VacancyService
        List<VacancyDto> vacancies = interviewFC
                .getVacanciesByUser(user.getUserId(), null, null);

        StringBuilder out = new StringBuilder("User: ").append(user.getUsername()).append("\n");

        for (VacancyDto vacancy : vacancies) {
            // 3) Скилы для вакансии
            List<SkillDto> skills = interviewFC
                    .getPagingSkillsByVacancyId(vacancy.getId(), null, null);

            // 4) Средний прогресс по вакансии
            double vacancyAvg = skills.stream()
                    .mapToDouble(skill -> {
                        List<TopicDto> topics = interviewFC
                                .findTopicsBySkill(skill.getId(), null, null);
                        return topics.stream()
                                .mapToInt(t -> progressByTopic.getOrDefault(t.getId(), 0))
                                .average()
                                .orElse(0);
                    })
                    .average()
                    .orElse(0);

            out.append(vacancy.getTitle()).append("[")
                    .append(vacancy.getId())
                    .append("]: ")
                    .append(String.format("%.0f%%", vacancyAvg))
                    .append("\n");

            // 5) Детализация по скилам и топикам
            for (SkillDto skill : skills) {
                List<TopicDto> topics = interviewFC
                        .findTopicsBySkill(skill.getId(), null, null);

                double skillAvg = topics.stream()
                        .mapToInt(t -> progressByTopic.getOrDefault(t.getId(), 0))
                        .average()
                        .orElse(0);

                out.append("  ")
                        .append(skill.getName())
                        .append(": ")
                        .append(String.format("%.0f%%", skillAvg))
                        .append("\n");

                for (TopicDto topic : topics) {
                    int level = progressByTopic.getOrDefault(topic.getId(), 0);
                    out.append("    ")
                            .append(topic.getName())
                            .append(": ")
                            .append(level)
                            .append("%\n");
                }
            }
        }

        return out.toString();
    }

    @Override
    public String getHandlerListName() {
        return Command.PROGRESS.getCommandText();
    }
}
