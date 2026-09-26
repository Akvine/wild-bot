package ru.akvine.wild.bot.infrastructure.outbox;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Relay outbox: периодически забирает записанные сообщения и доставляет их через {@link OutboxHandler}.
 * <ul>
 *     <li>несколько инстансов не мешают друг другу: сообщения забираются с блокировкой
 *     {@code skip locked} и закрепляются за relay'ем на время аренды;</li>
 *     <li>если доставка не удалась, сообщение повторяется с удваивающейся паузой; после
 *     {@code maxAttempts} попыток оно получает статус {@link OutboxStatus#FAILED} и пишется в лог на ERROR;</li>
 *     <li>если relay упал, не доставив взятые сообщения, они вернутся в очередь по истечении аренды;</li>
 *     <li>доставка - «минимум один раз»; порядок сообщений строго не гарантируется.</li>
 * </ul>
 */
@Slf4j
public class OutboxRelay {
    private final OutboxStore store;
    private final OutboxProperties properties;
    private final Map<String, OutboxHandler> handlers = new HashMap<>();

    public OutboxRelay(OutboxStore store, List<OutboxHandler> handlers, OutboxProperties properties) {
        this.store = store;
        this.properties = properties;
        handlers.forEach(handler -> {
            if (this.handlers.put(handler.type(), handler) != null) {
                throw new IllegalStateException("Several outbox handlers for type [" + handler.type() + "]");
            }
        });
    }

    @Scheduled(fixedDelayString = "${outbox.relay.interval.milliseconds:5000}")
    public void poll() {
        try {
            process();
        } catch (Exception e) {
            logger.error("Outbox relay failed", e);
        }
    }

    /**
     * Один проход relay'я
     *
     * @return сколько сообщений было взято в обработку
     */
    public int process() {
        int released = store.releaseExpiredLeases();
        if (released > 0) {
            logger.warn("Returned {} outbox message(s) to the queue: their processing did not finish in time", released);
        }

        List<OutboxMessage> batch = store.claimBatch(properties.getBatchSize(), properties.lease());
        for (OutboxMessage message : batch) {
            try {
                deliver(message);
            } catch (RuntimeException e) {
                // например, не удалось записать результат в БД: остальные сообщения пачки это не должно остановить;
                // само сообщение вернётся в очередь по истечении аренды
                logger.error("Can't finish processing outbox message #{}", message.id(), e);
            }
        }
        return batch.size();
    }

    @Scheduled(fixedDelayString = "${outbox.cleanup.interval.milliseconds:3600000}")
    public void cleanup() {
        try {
            int deleted =
                    store.deleteSentBefore(LocalDateTime.now().minusDays(properties.getSentRetentionDays()));
            if (deleted > 0) {
                logger.info("Deleted {} delivered outbox message(s)", deleted);
            }
        } catch (Exception e) {
            logger.error("Can't clean up outbox", e);
        }
    }

    private void deliver(OutboxMessage message) {
        OutboxHandler handler = handlers.get(message.type());
        if (handler == null) {
            logger.error("No outbox handler for type [{}], message #{} is failed", message.type(), message.id());
            store.markFailed(message.id(), message.attempts(), "No handler for type " + message.type());
            return;
        }

        try {
            handler.handle(message);
        } catch (Exception e) {
            onFailure(message, e);
            return;
        }
        store.markSent(message.id());
        logger.debug("Outbox message #{} [{}] delivered", message.id(), message.type());
    }

    private void onFailure(OutboxMessage message, Exception exception) {
        int attempts = message.attempts() + 1;
        if (attempts >= properties.getMaxAttempts()) {
            logger.error(
                    "Outbox message #{} [{}] is not delivered after {} attempts and will not be retried",
                    message.id(),
                    message.type(),
                    attempts,
                    exception);
            store.markFailed(message.id(), attempts, describe(exception));
            return;
        }

        Duration pause = properties.backoff(attempts);
        logger.warn(
                "Outbox message #{} [{}] delivery failed (attempt {}/{}), retry in {} s: {}",
                message.id(),
                message.type(),
                attempts,
                properties.getMaxAttempts(),
                pause.toSeconds(),
                exception.toString());
        store.markRetry(message.id(), attempts, LocalDateTime.now().plus(pause), describe(exception));
    }

    private static String describe(Exception exception) {
        return exception.getClass().getSimpleName() + ": " + exception.getMessage();
    }
}
