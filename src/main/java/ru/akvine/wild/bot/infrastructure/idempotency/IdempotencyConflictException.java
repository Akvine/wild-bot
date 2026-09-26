package ru.akvine.wild.bot.infrastructure.idempotency;

/**
 * Операция с этим ключом идемпотентности ещё выполняется: повторный запрос нужно повторить позже
 */
public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException(String key) {
        super("Request with idempotency key [" + key + "] is still being processed");
    }
}
