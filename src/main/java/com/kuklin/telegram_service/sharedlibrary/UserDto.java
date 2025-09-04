package com.kuklin.telegram_service.sharedlibrary;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class UserDto {
    private Long id;
    private String name;
    private String jobTitle;
    private BigDecimal balance;
    private String properties;

}
