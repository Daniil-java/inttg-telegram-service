package com.kuklin.telegram_service.sharedlibrary;

import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class VacancyDto {
    private Long id;
    private String title;
    private Long userId;
}
