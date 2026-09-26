package ru.akvine.wild.bot.infrastructure.outbox;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;

/**
 * Настройки outbox (префикс {@code outbox})
 */
@Getter
@Setter
public class OutboxProperties {
    /** Сколько сообщений relay берёт за один проход */
    private int batchSize = 50;
    /** После скольких неудачных попыток сообщение считается недоставляемым */
    private int maxAttempts = 10;
    /** Пауза после первой неудачи; дальше удваивается */
    private long initialBackoffSeconds = 10;
    /** Верхняя граница паузы между попытками */
    private long maxBackoffSeconds = 3600;
    /** На сколько сообщение закрепляется за relay'ем; если тот упал, по истечении оно вернётся в очередь */
    private long leaseSeconds = 120;
    /** Сколько дней хранить доставленные сообщения */
    private long sentRetentionDays = 7;

    public Duration lease() {
        return Duration.ofSeconds(leaseSeconds);
    }

    /**
     * @param attempts сколько попыток уже не удалось (начиная с 1)
     * @return пауза перед следующей попыткой: {@code initial * 2^(attempts-1)}, не больше максимума
     */
    public Duration backoff(int attempts) {
        double seconds = initialBackoffSeconds * Math.pow(2, Math.max(0, attempts - 1));
        return Duration.ofSeconds((long) Math.min(seconds, maxBackoffSeconds));
    }
}
