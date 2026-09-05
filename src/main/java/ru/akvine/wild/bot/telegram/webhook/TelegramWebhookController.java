package ru.akvine.wild.bot.telegram.webhook;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.telegram.bot.TelegramProductionBot;

@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "telegram.bot.type", havingValue = "webhook")
public class TelegramWebhookController implements TelegramWebhookControllerMeta {
    private final TelegramProductionBot telegramBot;
    private final TelegramWebhookValidator telegramWebhookValidator;

    @Override
    public BotApiMethod<?> onUpdateReceived(String botSecret, Update update) {
        telegramWebhookValidator.verifySecret(botSecret);
        return telegramBot.onWebhookUpdateReceived(update);
    }
}
