package ru.akvine.wild.bot.job.monitoring;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Периодически пишет в лог основные метрики пула соединений HikariCP: имя пула, общее число
 * соединений, число занятых (busy) и число свободных (idle) — без этого при исчерпании пула
 * (например, из-за зависшей транзакции) в логах не остаётся никакого следа до самого момента
 * ошибки "Connection is not available, request timed out".
 */
@RequiredArgsConstructor
@Slf4j
public class HikariPoolMetricsJob {
    private final HikariDataSource hikariDataSource;

    @Scheduled(fixedDelayString = "${hikari.pool.metrics.log.cron.milliseconds}")
    public void logPoolMetrics() {
        HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
        if (poolMXBean == null) {
            logger.warn(
                    "HikariCP pool = [{}] is not initialized yet, skip metrics logging",
                    hikariDataSource.getPoolName());
            return;
        }

        logger.info(
                "HikariCP pool = [{}]: totalConnections = [{}], busyConnections = [{}], idleConnections = [{}]",
                hikariDataSource.getPoolName(),
                poolMXBean.getTotalConnections(),
                poolMXBean.getActiveConnections(),
                poolMXBean.getIdleConnections());
    }
}
