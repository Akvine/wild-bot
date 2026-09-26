package ru.akvine.wild.bot.infrastructure.monitoring;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Пишет в отдельный логгер (в logback-spring*.xml он направлен в logs/slow-queries.log) SQL,
 * выполнявшийся дольше порога. Порог {@code <= 0} выключает мониторинг.
 */
@Slf4j(topic = SlowQueryLogger.LOGGER_NAME)
public class SlowQueryLogger {
    public static final String LOGGER_NAME = "ru.akvine.wild.bot.monitoring.SlowQuery";

    private final long thresholdMillis;

    public SlowQueryLogger(long thresholdMillis) {
        this.thresholdMillis = thresholdMillis;
    }

    /**
     * @param startedAtNanos значение {@link System#nanoTime()} на момент начала выполнения
     */
    void logIfSlow(String sql, long startedAtNanos) {
        if (thresholdMillis <= 0) {
            return;
        }

        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAtNanos);
        if (elapsedMillis >= thresholdMillis) {
            logger.warn("Slow query took [{}] ms (threshold = [{}] ms): {}", elapsedMillis, thresholdMillis, sql);
        }
    }
}
