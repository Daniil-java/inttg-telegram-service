package com.kuklin.telegram_service.sharedlibrary;

import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class TopicDto {
    private Long id;
    private String name;

}
