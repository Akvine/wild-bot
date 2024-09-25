package ru.akvine.wild.bot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.wild.bot.enums.NotificationProviderType;
import ru.akvine.wild.bot.services.notification.NotificationProvider;
import ru.akvine.wild.bot.services.notification.dummy.ConstantNotificationProvider;
import ru.akvine.wild.bot.services.notification.dummy.LogNotificationProvider;

@Configuration
@RequiredArgsConstructor
public class NotificationProviderConfig {
    @Value("${telegram.bot.dev.mode.enabled}")
    private boolean devModeEnabled;
    @Value("${notification.provider.type}")
    private String providerType;

    @Bean
    public NotificationProvider notificationProvider() {
        NotificationProviderType type = NotificationProviderType.safeValueOf(providerType);

        // TODO : слишком сложная и громоздкая нстройка. Придумать что-то по лучше
        if (devModeEnabled) {
            if (type.isDummy()) {
                switch (type) {
                    case CONSTANT: return new ConstantNotificationProvider();
                    default: return new LogNotificationProvider();
                }
            } else {
                throw new IllegalArgumentException("Provider type for dev mode can't be not dummy! Type = " + type.name());
            }
        } else {
            if (type.isDummy()) {
                throw new IllegalArgumentException("Provider type for real mode can't be dummy! Type = " + type.name());
            } else {
                return new LogNotificationProvider();
            }
        }
    }
}
