package com.kuklin.telegram_service.integrations;

import com.kuklin.telegram_service.configurations.FeignClientConfig;
import com.kuklin.telegram_service.sharedlibrary.BalanceSubtractRequest;
import com.kuklin.telegram_service.sharedlibrary.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        value = "user-service-feign-client",
        url = "${integrations.user-service.url}",
        configuration = FeignClientConfig.class
)
public interface UserServiceFeignClient {
    @RequestMapping(
            method = RequestMethod.PATCH,
            value = "/api/v1/users/{userId}/balance",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    UserDto subtractBalance(
            @PathVariable("userId") Long userId,
            @RequestBody BalanceSubtractRequest subtractTokens
    );

    @GetMapping("/api/v1/users/{userId}")
    UserDto getUserById(@PathVariable Long userId);

    @PostMapping("/api/v1/users/")
    UserDto createUser(@RequestBody UserDto userDto);

    @PutMapping("/api/v1/users/api/v1/users/")
    UserDto updateUser(@RequestBody UserDto userDto);
    @PutMapping("/api/v1/users/{userId}/job-title")
    UserDto setJobTitle(@PathVariable Long userId, @RequestBody String jobTitle);

    @PutMapping("/api/v1/users/{userId}/properties")
    UserDto setProperties(@PathVariable Long userId, @RequestBody String properties);

}
