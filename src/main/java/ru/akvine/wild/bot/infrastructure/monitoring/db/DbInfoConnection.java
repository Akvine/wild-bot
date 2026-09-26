package ru.akvine.wild.bot.infrastructure.monitoring.db;

/**
 * Описание подключения к БД для JMX. Пароль сюда не попадает
 */
public class DbInfoConnection implements DbConnectionMXBean {
    private String schema;
    private String host;
    private String port;
    private String serviceName;

    @Override
    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return "DbInfoConnection{schema='" + schema + "', host='" + host + "', port='" + port + "', serviceName='"
                + serviceName + "'}";
    }
}
