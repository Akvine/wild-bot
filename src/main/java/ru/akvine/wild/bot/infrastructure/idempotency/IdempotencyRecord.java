package ru.akvine.wild.bot.infrastructure.idempotency;

import java.time.LocalDateTime;

/**
 * Запись о ключе идемпотентности
 *
 * @param key ключ
 * @param fingerprint отпечаток запроса: тот же ключ с другим отпечатком - это не повтор, а ошибка клиента
 * @param status состояние операции
 * @param payload сохранённый результат операции (для {@link IdempotencyStatus#COMPLETED}), иначе {@code null}
 * @param expiresAt когда запись перестаёт действовать
 */
public record IdempotencyRecord(
        String key, String fingerprint, IdempotencyStatus status, String payload, LocalDateTime expiresAt) {

    public boolean isExpired(LocalDateTime now) {
        return expiresAt != null && !expiresAt.isAfter(now);
    }
}
