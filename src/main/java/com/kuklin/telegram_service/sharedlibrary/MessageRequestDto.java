package com.kuklin.telegram_service.sharedlibrary;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MessageRequestDto {
    @NotNull(message = "У сообщения должно быть содержание")
    private String content;
    private Long conversationId;
    private Float temperature;
    private ChatModel model;
    @NotNull(message = "В запросе должен содержаться идентификатор пользователя")
    private Long userId;

    public static MessageRequestDto getDefault(String content, Long conversationId) {
        return new MessageRequestDto()
                .setContent(content)
                .setConversationId(conversationId)
                .setModel(ChatModel.GPT4O);
    }

    public static MessageRequestDto getServiceMessage(String content) {
        return new MessageRequestDto()
                .setContent(String.format(content))
                .setModel(ChatModel.GPT4O);
    }
}
