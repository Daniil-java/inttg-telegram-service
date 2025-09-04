package com.kuklin.telegram_service.sharedlibrary.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus {
    AI_CONNECTION_ERROR(HttpStatus.BAD_REQUEST, "AI connection error!"),
    CONVERSATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "Conversation not found!"),
    CONVERSATION_SPECIFIED_ERROR(HttpStatus.BAD_REQUEST, "The conversation must be specified"),
    MODEL_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "Model is not supported"),
    PROVIDER_NOT_FOUND(HttpStatus.BAD_REQUEST, "Provider not found!"),
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "User not found!"),
    USER_INSUFFICIENT_FUNDS(HttpStatus.BAD_REQUEST, "Insufficient funds in the account!"),
    USER_CREATION_ERROR(HttpStatus.BAD_REQUEST, "User ID must be null, when you create new user!"),
    VACANCY_NOT_FOUND(HttpStatus.BAD_REQUEST, "Vacancy not found!");

    private HttpStatus httpStatus;
    private String message;
}
