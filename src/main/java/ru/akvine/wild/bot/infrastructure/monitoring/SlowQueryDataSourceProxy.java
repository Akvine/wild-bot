package ru.akvine.wild.bot.infrastructure.monitoring;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Обёртка над {@link DataSource}: отдаёт соединения, замеряющие время выполнения каждого запроса
 * (см. {@link SlowQueryConnectionProxy}). Не зависит от Hibernate - видит и JPA, и JdbcTemplate,
 * и Liquibase, и любые другие обращения к базе через этот DataSource.
 */
public class SlowQueryDataSourceProxy implements DataSource {
    private final DataSource targetDataSource;
    private final SlowQueryLogger slowQueryLogger;

    public SlowQueryDataSourceProxy(DataSource targetDataSource, SlowQueryLogger slowQueryLogger) {
        this.targetDataSource = targetDataSource;
        this.slowQueryLogger = slowQueryLogger;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new SlowQueryConnectionProxy(targetDataSource.getConnection(), slowQueryLogger);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return new SlowQueryConnectionProxy(targetDataSource.getConnection(username, password), slowQueryLogger);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(targetDataSource)) {
            return iface.cast(targetDataSource);
        }
        return targetDataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(targetDataSource) || targetDataSource.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return targetDataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        targetDataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        targetDataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return targetDataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return targetDataSource.getParentLogger();
    }
}
