package ru.akvine.wild.bot.infrastructure.outbox;

/**
 * Состояние сообщения в outbox
 */
public enum OutboxStatus {
    /** Ждёт отправки (или повторной попытки) */
    PENDING,
    /** Взято relay'ем в обработку; если тот упал, по истечении аренды вернётся в {@link #PENDING} */
    PROCESSING,
    /** Доставлено */
    SENT,
    /** Все попытки исчерпаны: нужно вмешательство человека */
    FAILED
}
