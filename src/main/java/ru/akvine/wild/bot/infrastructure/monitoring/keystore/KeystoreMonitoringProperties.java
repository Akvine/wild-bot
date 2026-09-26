package ru.akvine.wild.bot.infrastructure.monitoring.keystore;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Настройки слежения за сроком действия сертификатов (префикс {@code monitoring.keystore})
 */
@Getter
@Setter
public class KeystoreMonitoringProperties {
    private boolean enabled = false;
    /** За сколько дней до окончания срока действия сертификат попадает в отчёт */
    private int daysToExpire = 120;
    /** Сколько дней после окончания срока действия сертификат ещё показывается в отчёте */
    private int daysExpired = 30;
    /** Как часто проверять ключницы, в часах */
    private long checkIntervalHours = 24;

    private List<Keystore> keystores = new ArrayList<>();

    @Getter
    @Setter
    public static class Keystore {
        /** Путь к ключнице, например {@code classpath:ssl/keystore.pfx} или {@code file:/opt/certs/store.jks} */
        private String location;

        private String password;
        /** JKS, JCEKS или PKCS12; если не задан, определяется по расширению файла */
        private String type;

        private String provider;
    }
}
