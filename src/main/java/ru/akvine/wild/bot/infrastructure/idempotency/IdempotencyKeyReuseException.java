package ru.akvine.wild.bot.infrastructure.idempotency;

/**
 * Ключ идемпотентности уже использован для другого запроса: это ошибка клиента, а не повтор
 */
public class IdempotencyKeyReuseException extends RuntimeException {
    public IdempotencyKeyReuseException(String key) {
        super("Idempotency key [" + key + "] was already used with a different request");
    }
}
