package ru.akvine.wild.bot.infrastructure.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Удаляет истёкшие ключи идемпотентности из БД (в памяти они чистятся сами, в Redis - по сроку жизни
 * ключа)
 */
@RequiredArgsConstructor
@Slf4j
public class IdempotencyCleanupJob {
    private final DatabaseIdempotencyStore store;

    @Scheduled(fixedDelayString = "${idempotency.cleanup.interval.milliseconds:3600000}")
    public void deleteExpired() {
        try {
            int deleted = store.deleteExpired();
            if (deleted > 0) {
                logger.info("Deleted {} expired idempotency keys", deleted);
            }
        } catch (Exception e) {
            logger.error("Can't delete expired idempotency keys", e);
        }
    }
}
