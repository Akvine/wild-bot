package ru.akvine.wild.bot.infrastructure.monitoring.pool;

import static ru.akvine.commons.util.ScheduledExecutors.newSingleThreadScheduledExecutor;
import static ru.akvine.commons.util.Threads.newThreadFactory;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

/**
 * Периодически пишет в лог состояние пула соединений (всего/занято/свободно)
 */
@Slf4j
public class ConnectionPoolMonitor {
    private final MonitorableConnectionPool monitorableConnectionPool;
    private final long intervalMillis;
    private final ScheduledExecutorService stateMonitorExecutorService;

    public ConnectionPoolMonitor(DataSource dataSource, long intervalMillis, String poolName) {
        Objects.requireNonNull(dataSource, "dataSource");

        if (intervalMillis < 1000) {
            throw new IllegalArgumentException(
                    "intervalMillis must be more that 1 second but was " + intervalMillis + " millis");
        }
        this.intervalMillis = intervalMillis;

        monitorableConnectionPool = MonitorableConnectionPoolFactory.create(dataSource, poolName);
        stateMonitorExecutorService = newSingleThreadScheduledExecutor(newThreadFactory("connection-pool-logger"));
    }

    public void start() {
        enableConnectionAcquiringMonitor();
        enableConnectionPoolStateMonitor(intervalMillis);
    }

    public void stop() {
        stateMonitorExecutorService.shutdownNow();
    }

    private void enableConnectionPoolStateMonitor(long intervalMillis) {
        stateMonitorExecutorService.scheduleWithFixedDelay(
                this::logDatabaseConnectionPoolState, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
    }

    private void enableConnectionAcquiringMonitor() {
        monitorableConnectionPool.enableConnectionAcquiringMonitor();
    }

    private void logDatabaseConnectionPoolState() {
        try {
            int totalConnections = monitorableConnectionPool.getTotalConnections();
            if (totalConnections == MonitorableConnectionPool.POOL_NOT_YET_INITIALIZED_VALUE) {
                logger.warn(
                        "CONNECTION_POOL_STATE [{}]: pool not initialized yet",
                        monitorableConnectionPool.getPoolName());
                return;
            }

            logger.info(
                    "CONNECTION_POOL_STATE [{}]: total: {} ; busy: {} ; idle: {}",
                    monitorableConnectionPool.getPoolName(),
                    monitorableConnectionPool.getTotalConnections(),
                    monitorableConnectionPool.getBusyConnections(),
                    monitorableConnectionPool.getIdleConnections());
        } catch (Exception e) {
            logger.error("Failed to get pool state info", e);
        }
    }
}
