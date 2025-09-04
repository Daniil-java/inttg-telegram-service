package com.kuklin.telegram_service.integrations;

import com.kuklin.telegram_service.configurations.FeignClientConfig;
import com.kuklin.telegram_service.models.SpeechRequest;
import com.kuklin.telegram_service.models.TranscriptionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;

@FeignClient(
        value = "open-ai-feign-client",
        url = "${integrations.openai-api.url}",
        configuration = FeignClientConfig.class
)
public interface OpenAiFeignClient {

    @PostMapping(value = "audio/transcriptions",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    TranscriptionResponse transcribeAudio(
            @RequestHeader("Authorization") String key,
            @RequestPart("file") Resource file,
            @RequestPart("model") String model
    );

    @PostMapping("audio/speech")
    byte[] makeSpeech(
            @RequestHeader("Authorization") String key,
            @RequestBody SpeechRequest speechRequest
    );

}
