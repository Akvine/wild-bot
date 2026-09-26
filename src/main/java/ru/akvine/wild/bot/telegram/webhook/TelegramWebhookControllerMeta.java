package ru.akvine.wild.bot.telegram.webhook;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.infrastructure.httplogging.annotation.DoNotLogRequest;

@RequestMapping(value = "/telegram/bot")
public interface TelegramWebhookControllerMeta {

    // секрет бота передаётся в пути, поэтому запрос в лог не пишем
    @DoNotLogRequest
    @PostMapping(value = "/{botSecret}")
    BotApiMethod<?> onUpdateReceived(@PathVariable("botSecret") String botSecret, @RequestBody Update update);
}
