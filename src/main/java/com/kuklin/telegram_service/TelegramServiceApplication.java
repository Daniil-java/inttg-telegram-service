package com.kuklin.telegram_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TelegramServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelegramServiceApplication.class, args);
	}

}
