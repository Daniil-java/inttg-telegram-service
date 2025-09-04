package com.kuklin.telegram_service.sharedlibrary;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class MessageResponseDto {
    private String content;
    private String errorDetails;
    private BigDecimal inputToken;
    private BigDecimal outputToken;

}
