package com.kuklin.telegram_service.telegram.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Command {
    START("/start"),
    JOB("/job"),
    IF("/if"),
    START_INTERVIEW("/start_interview"),
    END("/end"), TOPIC_INTERVIEW_END("/topic_interview_end"),
    HISTORY("/history"),
    RESULTS("/results"),
    ERROR("error"),
    INTERVIEW("interview"),
    URL_PROCESS("url_process"),
    RANDOM_INTERVIEW("/random_interview"),
    COACH("/coach"),
    PROGRESS("/progress"),
    SKILL("/skills"),
    TOPIC("/topic"),
    TOPIC_INTERVIEW("/topicI");

    private final String commandText;
}
