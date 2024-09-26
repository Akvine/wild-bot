package ru.akvine.wild.bot.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.wild.bot.enums.security.TwoFactorNotificationSenderType;
import ru.akvine.wild.bot.services.notification.TwoFactorNotificationSender;
import ru.akvine.wild.bot.services.notification.dummy.ConstantTwoFactorNotificationSender;
import ru.akvine.wild.bot.services.notification.dummy.LogTwoFactorNotificationSender;

@Configuration
@RequiredArgsConstructor
public class TwoFactorNotificationProviderConfig {
    @Value("${telegram.bot.dev.mode.enabled}")
    private boolean devModeEnabled;
    @Value("${security.notification.provider.type}")
    private String providerType;

    @Bean
    public TwoFactorNotificationSender notificationProvider() {
        TwoFactorNotificationSenderType type = TwoFactorNotificationSenderType.safeValueOf(providerType);

        // TODO : слишком сложная и громоздкая нстройка. Придумать что-то по лучше
        if (devModeEnabled) {
            if (type.isDummy()) {
                switch (type) {
                    case CONSTANT: return new ConstantTwoFactorNotificationSender();
                    default: return new LogTwoFactorNotificationSender();
                }
            } else {
                throw new IllegalArgumentException("Provider type for dev mode can't be not dummy! Type = " + type.name());
            }
        } else {
            if (type.isDummy()) {
                throw new IllegalArgumentException("Provider type for real mode can't be dummy! Type = " + type.name());
            } else {
                return new LogTwoFactorNotificationSender();
            }
        }
    }
}
