package ru.akvine.wild.bot.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.wild.bot.enums.security.TwoFactorNotificationSenderType;
import ru.akvine.wild.bot.services.notification.TwoFactorNotificationSender;

@Configuration
@RequiredArgsConstructor
public class TwoFactorNotificationProviderConfig {
    @Value("${security.notification.provider.type}")
    private String providerType;

    @Bean
    public TwoFactorNotificationSender notificationProvider() {
        return TwoFactorNotificationSenderType.resolveByCodeAndBotType(providerType);
    }
}
