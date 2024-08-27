package ru.akvine.marketspace.bot.telegram.webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.akvine.marketspace.bot.exceptions.BadCredentialsException;

@Component
public class TelegramWebhookValidator {
    @Value("${telegram.bot.secret}")
    private String telegramBotSecret;

    public void verifySecret(String botSecret) {
        if (!telegramBotSecret.equals(botSecret)) {
            throw new BadCredentialsException("Invalid bot secret!");
        }
    }
}
