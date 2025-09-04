package com.kuklin.telegram_service.sharedlibrary;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class InterviewRequest {
    private Long conversationId;
    private Long userId;
    private String result;
}
