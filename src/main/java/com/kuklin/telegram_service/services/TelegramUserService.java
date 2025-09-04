package com.kuklin.telegram_service.services;

import com.kuklin.telegram_service.entities.TelegramUser;
import com.kuklin.telegram_service.integrations.UserServiceFeignClient;
import com.kuklin.telegram_service.repositories.TelegramUserRepository;
import com.kuklin.telegram_service.sharedlibrary.UserDto;
import com.kuklin.telegram_service.telegram.utils.BotState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramUserService {
    private static final String TG_USERNAME_PREFIX = "tg@";
    private final TelegramUserRepository telegramUserRepository;
    private final UserServiceFeignClient userServiceFC;

    public TelegramUser getTelegramUserByIdOrNull(Long telegramId) {
        return telegramUserRepository.findById(telegramId).orElse(null);
    }

    public TelegramUser createOrGetUserByTelegram(User telegramUser) {
        Optional<TelegramUser> optionalTelegramUser =
                telegramUserRepository.findById(telegramUser.getId());

        if (optionalTelegramUser.isPresent()) {
            return optionalTelegramUser.get();
        }

        //Создаем нового пользователя
        TelegramUser tgUser = TelegramUser.convertFromTelegram(telegramUser);
        tgUser.setBotState(BotState.WAIT);
        UserDto userDto = userServiceFC.createUser(
                new UserDto().setName(TG_USERNAME_PREFIX + tgUser.getUsername())
        );
        tgUser.setUserId(userDto.getId());
        return telegramUserRepository.save(tgUser);
    }

    public TelegramUser setBotStateByIdOrGetNull(Long telegramId, BotState state) {
        TelegramUser telegramUser = getUserOrNull(telegramId);
        if (telegramUser == null) return null;
        return telegramUserRepository.save(telegramUser.setBotState(state));
    }

    public TelegramUser setActualConversationIdOrGetNull(Long telegramId, Long conversationId) {
        TelegramUser telegramUser = getUserOrNull(telegramId);
        return telegramUser == null ? null :
                telegramUserRepository.save(telegramUser.setActualAiConversationId(conversationId));
    }

    public Long getActualConversationId(Long telegramId) {
        TelegramUser telegramUser = getUserOrNull(telegramId);
        return telegramUser == null ? null : telegramUser.getActualAiConversationId();
    }

    private TelegramUser getUserOrNull(Long telegramId) {
        Optional<TelegramUser> telegramUser = telegramUserRepository.findById(telegramId);
        if (!telegramUser.isPresent()) {
            return null;
        } else {
            return telegramUser.get();
        }
    }

    public TelegramUser save(TelegramUser telegramUser) {
        return telegramUserRepository.save(telegramUser);
    }
}
