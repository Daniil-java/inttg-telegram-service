package com.kuklin.telegram_service.integrations;

import com.kuklin.telegram_service.sharedlibrary.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        value = "interview-feign-client",
        url = "${integrations.interview-service.url}"
)
public interface InterviewFeignClient {
    // InterviewController
    @PostMapping("/api/v1/interview")
    InterviewDto createInterview(@RequestBody InterviewRequest interviewRequest);

    @PutMapping("/api/v1/interview")
    InterviewDto setInterviewResult(@RequestBody InterviewRequest interviewRequest);

    @GetMapping("/api/v1/interview")
    List<InterviewDto> getLatestResultList(@RequestParam Long userId);

    // SkillController
    @GetMapping("/api/v1/skill/{skillId}")
    SkillDto getSkillByIdOrNull(@PathVariable Long skillId);

    @GetMapping("/api/v1/skill")
    List<SkillDto> getPagingSkillsByVacancyId(
            @RequestParam Long vacancyId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer rowCount
    );

    // TopicController
    @GetMapping("/api/v1/topic/{topicId}")
    TopicDto getTopicByIdOrNull(@PathVariable Long topicId);

    @GetMapping("/api/v1/topic")
    List<TopicDto> findTopicsBySkill(
            @RequestParam Long skillId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer rowCount
    );

    // TopicProgressController
    @PutMapping("/api/v1/progress")
    TopicProgressDto updateProgress(@RequestBody TopicProgressDto dto);

    @GetMapping("/api/v1/progress")
    List<TopicProgressDto> getTopicProgressByUserId(@RequestParam Long userId);

    // VacancyController
    @PostMapping("/api/v1/vacancy")
    VacancyDto createVacancyName(@RequestBody VacancyDto vacancyDto);

    @GetMapping("/api/v1/vacancy")
    List<VacancyDto> getVacanciesByUser(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer rowCount
    );

    @GetMapping("/api/v1/vacancy/{vacancyId}")
    public VacancyDto getVacancyById(@PathVariable Long vacancyId);

}
