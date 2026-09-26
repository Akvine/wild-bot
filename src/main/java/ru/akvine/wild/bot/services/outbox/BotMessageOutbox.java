package ru.akvine.wild.bot.services.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxHandler;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxMessage;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxService;
import ru.akvine.wild.bot.services.integration.BotIntegrationAdapter;

/**
 * Надёжная отправка текстовых сообщений клиентам бота через transactional outbox. {@link #enqueue}
 * вызывается в транзакции, которая меняет данные (например, помечает подписку как «уведомлённую»), а
 * само сообщение уходит клиенту позже, с повторами при сбоях Telegram или Max.
 */
@Component
@RequiredArgsConstructor
public class BotMessageOutbox implements OutboxHandler {
    public static final String TYPE = "BOT_MESSAGE";

    private final OutboxService outboxService;
    private final BotIntegrationAdapter botIntegrationAdapter;

    /**
     * Ставит сообщение клиенту в очередь; обязано вызываться внутри транзакции
     *
     * @param dedupKey ключ дедупликации или {@code null}: сообщение с тем же ключом второй раз не добавится
     * @return {@code true}, если сообщение поставлено в очередь
     */
    public boolean enqueue(String chatId, BotType botType, String text, String dedupKey) {
        return outboxService.enqueue(TYPE, new BotMessagePayload(chatId, botType, text), dedupKey);
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void handle(OutboxMessage message) {
        BotMessagePayload payload = outboxService.read(message, BotMessagePayload.class);
        botIntegrationAdapter.sendMessage(payload.chatId(), payload.botType(), payload.text());
    }
}
