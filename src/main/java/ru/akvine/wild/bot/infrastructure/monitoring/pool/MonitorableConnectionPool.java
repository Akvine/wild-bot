package ru.akvine.wild.bot.infrastructure.monitoring.pool;

/**
 * Пул соединений, состояние которого можно опрашивать
 */
public interface MonitorableConnectionPool {
    int POOL_NOT_YET_INITIALIZED_VALUE = -1;

    String getPoolName();

    void enableConnectionAcquiringMonitor();

    int getTotalConnections();

    int getBusyConnections();

    int getIdleConnections();

    int getMaxPoolSize();
}
