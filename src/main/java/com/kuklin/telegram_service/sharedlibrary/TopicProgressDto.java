package com.kuklin.telegram_service.sharedlibrary;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

@Data
@Accessors(chain = true)
public class TopicProgressDto {
    private Long id;
    private Long userId;
    private Long topicId;
    private Integer confidenceLevel;
    private OffsetDateTime lastReviewed;
    private Boolean isWeakArea;

}
