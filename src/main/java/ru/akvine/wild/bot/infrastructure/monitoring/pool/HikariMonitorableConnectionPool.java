package ru.akvine.wild.bot.infrastructure.monitoring.pool;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.metrics.IMetricsTracker;
import com.zaxxer.hikari.metrics.PoolStats;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

/**
 * Реализация {@link MonitorableConnectionPool} для HikariCP: вешает на пул свой
 * {@link IMetricsTracker}, из которого берёт {@link PoolStats}.
 */
@Slf4j
class HikariMonitorableConnectionPool implements MonitorableConnectionPool {
    private final HikariDataSource dataSource;
    private final String poolName;
    private final AtomicReference<MetricsTracker> metricsTrackerRef = new AtomicReference<>();

    HikariMonitorableConnectionPool(DataSource dataSource, String poolName) {
        Objects.requireNonNull(dataSource, "dataSource");
        if (!(dataSource instanceof HikariDataSource)) {
            throw new IllegalArgumentException(
                    "Object of class [" + dataSource.getClass().getName() + "] must be an instance of "
                            + HikariDataSource.class);
        }

        this.dataSource = (HikariDataSource) dataSource;
        this.poolName = Objects.requireNonNull(poolName, "poolName");
    }

    @Override
    public String getPoolName() {
        return poolName;
    }

    @Override
    public void enableConnectionAcquiringMonitor() {
        dataSource.setMetricsTrackerFactory((internalPoolName, poolStats) -> {
            MetricsTracker metricsTracker = new MetricsTracker(poolStats);
            metricsTrackerRef.set(metricsTracker);
            return metricsTracker;
        });
    }

    @Override
    public int getTotalConnections() {
        return Optional.ofNullable(metricsTrackerRef.get())
                .map(MetricsTracker::getTotalConnections)
                .orElse(POOL_NOT_YET_INITIALIZED_VALUE);
    }

    @Override
    public int getBusyConnections() {
        return Optional.ofNullable(metricsTrackerRef.get())
                .map(MetricsTracker::getBusyConnections)
                .orElse(POOL_NOT_YET_INITIALIZED_VALUE);
    }

    @Override
    public int getIdleConnections() {
        return Optional.ofNullable(metricsTrackerRef.get())
                .map(MetricsTracker::getIdleConnections)
                .orElse(POOL_NOT_YET_INITIALIZED_VALUE);
    }

    @Override
    public int getMaxPoolSize() {
        return Optional.ofNullable(metricsTrackerRef.get())
                .map(MetricsTracker::getMaxPoolSize)
                .orElse(POOL_NOT_YET_INITIALIZED_VALUE);
    }

    class MetricsTracker implements IMetricsTracker {
        private final PoolStats poolStats;

        MetricsTracker(PoolStats poolStats) {
            this.poolStats = poolStats;
        }

        int getTotalConnections() {
            return poolStats.getTotalConnections();
        }

        int getBusyConnections() {
            return poolStats.getActiveConnections();
        }

        int getIdleConnections() {
            return poolStats.getIdleConnections();
        }

        int getMaxPoolSize() {
            return poolStats.getMaxConnections();
        }

        @Override
        public void recordConnectionCreatedMillis(long connectionCreatedMillis) {
            logger.debug("Got DB connection in [{}] ms for [{}].", connectionCreatedMillis, poolName);
        }
    }
}
