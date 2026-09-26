package ru.akvine.wild.bot.infrastructure.monitoring.db;

/**
 * Куда подключено приложение: атрибуты видны в JMX
 */
public interface DbConnectionMXBean {
    String getHost();

    String getPort();

    String getSchema();

    String getServiceName();
}
