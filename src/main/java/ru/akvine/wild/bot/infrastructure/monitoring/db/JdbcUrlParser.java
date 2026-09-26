package ru.akvine.wild.bot.infrastructure.monitoring.db;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;

/**
 * Достаёт хост, порт и имя базы из JDBC URL вида {@code jdbc:postgresql://host:5432/db?...}
 */
@Slf4j
public class JdbcUrlParser {

    /**
     * @param jdbcUrl JDBC URL
     * @param schema пользователь/схема, под которой идёт подключение
     * @return описание подключения или {@code null}, если URL не в формате {@code //host:port/name}
     *         (например, встроенная база {@code jdbc:h2:mem:...})
     */
    public DbInfoConnection parseUrl(String jdbcUrl, String schema) {
        try {
            int authorityStart = jdbcUrl.indexOf("//");
            if (authorityStart < 0) {
                logger.debug("JDBC URL has no host part, skipping: {}", jdbcUrl);
                return null;
            }
            URI uri = URI.create(jdbcUrl.substring(authorityStart));

            DbInfoConnection infoConnection = new DbInfoConnection();
            infoConnection.setHost(uri.getHost());
            infoConnection.setPort(uri.getPort() < 0 ? null : String.valueOf(uri.getPort()));
            infoConnection.setSchema(schema);
            infoConnection.setServiceName(
                    uri.getPath() == null ? null : uri.getPath().replace("/", ""));
            return infoConnection;
        } catch (Exception e) {
            logger.warn("Failed to parse JDBC URL: {}", jdbcUrl);
            return null;
        }
    }
}
