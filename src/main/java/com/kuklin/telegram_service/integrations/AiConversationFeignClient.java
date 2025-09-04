package com.kuklin.telegram_service.integrations;

import com.kuklin.telegram_service.sharedlibrary.ConversationDto;
import com.kuklin.telegram_service.sharedlibrary.MessageRequestDto;
import com.kuklin.telegram_service.sharedlibrary.MessageResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        value = "ai-conversation-feign-client",
        url = "${integrations.ai-conversation-service.url}"
)
public interface AiConversationFeignClient {
    @PostMapping("/api/v1/conversations")
    ConversationDto postNewConversationDto(@RequestBody ConversationDto conversationDto);

    @GetMapping("/api/v1/conversations/{id}")
    ConversationDto getConversationDtoByIdOrGetNull(@PathVariable Long id);

    @PostMapping("/api/v1/messages/service")
    String sendServiceMessage(
            @RequestBody @Validated MessageRequestDto messageRequestDto);

    @PostMapping("/api/v1/messages/")
    MessageResponseDto sendUserMessage(
            @RequestBody @Validated MessageRequestDto messageRequestDto);

}
