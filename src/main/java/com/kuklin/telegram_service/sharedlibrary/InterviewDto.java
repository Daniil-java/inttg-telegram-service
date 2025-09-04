package com.kuklin.telegram_service.sharedlibrary;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class InterviewDto {
    private Long id;
    private String jobTitle;
    private String result;
    private Long conversationId;
    private Long userId;
    private String properties;

}
