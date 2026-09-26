package ru.akvine.wild.bot.infrastructure.idempotency;

/**
 * Состояние операции, выполняемой под ключом идемпотентности
 */
public enum IdempotencyStatus {
    /** Операция выполняется прямо сейчас */
    IN_PROGRESS,
    /** Операция завершена, её результат сохранён */
    COMPLETED
}
