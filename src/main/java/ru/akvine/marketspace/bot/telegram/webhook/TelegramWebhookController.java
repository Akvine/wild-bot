package ru.akvine.marketspace.bot.telegram.webhook;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.telegram.bot.TelegramProductionBot;

@RestController
@RequiredArgsConstructor
@ConditionalOnExpression("${telegram.bot.enabled}.equals('true') && ${telegram.bot.dev.mode.enabled}.equals('false')")
public class TelegramWebhookController implements TelegramWebhookControllerMeta {
    private final TelegramProductionBot telegramBot;
    private final TelegramWebhookValidator telegramWebhookValidator;

    @Override
    public BotApiMethod<?> onUpdateReceived(String botSecret, Update update) {
        telegramWebhookValidator.verifySecret(botSecret);
        return telegramBot.onWebhookUpdateReceived(update);
    }
}
