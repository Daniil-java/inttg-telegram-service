package com.kuklin.telegram_service.entities;

import com.kuklin.telegram_service.telegram.utils.BotState;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "telegram_users")
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class TelegramUser {
    @Id
    private Long telegramId;
    private String username;
    private String firstname;
    private String lastname;
    private String languageCode;
    @Enumerated(EnumType.STRING)
    private BotState botState;
    private Long actualAiConversationId;
    private Long actualTopicId;
    private Long userId;
    @UpdateTimestamp
    private LocalDateTime updated;
    @CreationTimestamp
    private LocalDateTime created;

    private TelegramUser setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
        return this;
    }

    public static TelegramUser convertFromTelegram(User user) {
        return new TelegramUser()
                .setTelegramId(user.getId())
                .setUsername(user.getUserName())
                .setFirstname(user.getFirstName())
                .setLastname(user.getLastName())
                .setLanguageCode(user.getLanguageCode());
    }
}
