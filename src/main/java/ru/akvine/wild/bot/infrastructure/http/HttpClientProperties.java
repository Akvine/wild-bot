package ru.akvine.wild.bot.infrastructure.http;

import lombok.Getter;
import lombok.Setter;

/**
 * Настройки общего http-клиента и {@code RestTemplate} (префикс {@code http.client})
 */
@Getter
@Setter
public class HttpClientProperties {
    private boolean enabled = false;
    /** Имя клиента - попадает в логи запросов и retry-стратегии */
    private String name = "wild-bot";

    private int connectTimeoutMillis = 10000;
    private int readTimeoutMillis = 20000;
    private int connectionPoolSize = 50;
    /** {@code false} - не проверять соответствие имени хоста сертификату сервера */
    private boolean verifyHostname = true;
    /** {@code 0} - повторять только при потере соединения ({@code LostConnectionRetryHandler}) */
    private int retryCount = 0;

    private Keystore keystore = new Keystore();

    @Getter
    @Setter
    public static class Keystore {
        /**
         * Ключница с клиентскими и доверенными сертификатами, например {@code classpath:ssl/keystore.pfx}.
         * Если не задана, используется системный набор сертификатов JVM
         */
        private String location;

        private String password;
    }
}
