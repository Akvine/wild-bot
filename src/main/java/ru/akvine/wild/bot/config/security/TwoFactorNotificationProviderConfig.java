package ru.akvine.wild.bot.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.wild.bot.enums.security.TwoFactorNotificationSenderType;
import ru.akvine.wild.bot.services.notification.TwoFactorNotificationSender;
import ru.akvine.wild.bot.services.notification.dummy.ConstantTwoFactorNotificationSender;
import ru.akvine.wild.bot.services.notification.dummy.LogTwoFactorNotificationSender;

import java.util.Objects;

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

        // TODO : слишком сложная и громоздкая нстройка. Придумать что-то по лучше. Да и вообще нужно переделать с учетом настроек и MAX
        if (Objects.requireNonNull(type) == TwoFactorNotificationSenderType.CONSTANT) {
            return new ConstantTwoFactorNotificationSender();
        }
        return new LogTwoFactorNotificationSender();
    }
}
