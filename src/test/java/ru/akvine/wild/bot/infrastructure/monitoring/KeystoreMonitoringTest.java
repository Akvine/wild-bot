package ru.akvine.wild.bot.infrastructure.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import ru.akvine.wild.bot.infrastructure.monitoring.keystore.AbstractKeystoreObserver;
import ru.akvine.wild.bot.infrastructure.monitoring.keystore.ExpireCertData;
import ru.akvine.wild.bot.infrastructure.monitoring.keystore.FileKeyStoreObserver;
import ru.akvine.wild.bot.infrastructure.monitoring.keystore.KeyStoreConfig;
import ru.akvine.wild.bot.infrastructure.monitoring.keystore.KeyStoreObserver;
import ru.akvine.wild.bot.infrastructure.monitoring.keystore.KeystoreExpirationMonitor;
import ru.akvine.wild.bot.infrastructure.monitoring.keystore.KeystoreMonitoringProperties;
import ru.akvine.wild.bot.testsupport.TestKeystores;

@DisplayName("Слежение за сроком действия сертификатов")
class KeystoreMonitoringTest {
    private static final String PASSWORD = TestKeystores.PASSWORD;

    private static KeyStoreConfig config(Path path) {
        return new KeyStoreConfig(new FileSystemResource(path), PASSWORD);
    }

    @Test
    @DisplayName("Действующие сертификаты попадают в отчёт, только если истекают в пределах daysToExpire")
    void validCertificatesRespectDaysToExpire() {
        List<ExpireCertData> within =
                new FileKeyStoreObserver(List.of(config(TestKeystores.path())), 400, true, 30).observe();
        List<ExpireCertData> outside =
                new FileKeyStoreObserver(List.of(config(TestKeystores.path())), 120, true, 30).observe();

        assertThat(within)
                .extracting(ExpireCertData::alias)
                .containsExactlyInAnyOrder(TestKeystores.CLIENT_1, TestKeystores.CLIENT_2, TestKeystores.TRUST_1);
        assertThat(within).allSatisfy(cert -> {
            assertThat(cert.expiryIn()).isBetween(360, 366);
            assertThat(cert.hash()).hasSize(64);
            assertThat(cert.source()).contains("test.p12");
        });
        assertThat(outside).isEmpty();
    }

    @Test
    @DisplayName("Истёкшие сертификаты показываются по настройкам includeExpired и includeExpiredDays")
    void expiredCertificatesRespectSettings() {
        Path expired = TestKeystores.expiredPath();

        List<ExpireCertData> shown = new FileKeyStoreObserver(List.of(config(expired)), 30, true, 30).observe();
        List<ExpireCertData> tooOld = new FileKeyStoreObserver(List.of(config(expired)), 30, true, 2).observe();
        List<ExpireCertData> hidden = new FileKeyStoreObserver(List.of(config(expired)), 30, false, 30).observe();

        assertThat(shown).extracting(ExpireCertData::alias).containsExactly(TestKeystores.EXPIRED);
        assertThat(shown.get(0).expiryIn()).isNegative();
        assertThat(tooOld).isEmpty();
        assertThat(hidden).isEmpty();
    }

    @Test
    @DisplayName("Ошибка чтения одной ключницы не мешает остальным")
    void brokenKeystoreIsSkipped() {
        KeyStoreConfig broken = new KeyStoreConfig(new FileSystemResource("/no/such/file.p12"), PASSWORD);

        List<ExpireCertData> result =
                new FileKeyStoreObserver(List.of(broken, config(TestKeystores.path())), 400, true, 30).observe();

        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("Параметры проверяются: отрицательные значения запрещены")
    void validatesParameters() {
        assertThatThrownBy(() -> new FileKeyStoreObserver(List.of(), -1, true, 30))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FileKeyStoreObserver(List.of(), 30, true, -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AbstractKeystoreObserver.validateDaysToExpire(-5))
                .isInstanceOf(IllegalArgumentException.class);
        AbstractKeystoreObserver.validateDaysToExpire(0);
        AbstractKeystoreObserver.validateIncludeExpiredDays(0);
    }

    @Test
    @DisplayName("KeyStoreConfig определяет тип по расширению, провайдеру и явному типу")
    void keyStoreConfig() throws Exception {
        assertThat(KeyStoreConfig.KeyStoreType.getTypeByName("pfx")).isEqualTo(KeyStoreConfig.KeyStoreType.PKCS12);
        assertThat(KeyStoreConfig.KeyStoreType.getTypeByName("P12")).isEqualTo(KeyStoreConfig.KeyStoreType.PKCS12);
        assertThat(KeyStoreConfig.KeyStoreType.getTypeByName("jceks")).isEqualTo(KeyStoreConfig.KeyStoreType.JCEKS);
        assertThat(KeyStoreConfig.KeyStoreType.getTypeByName("unknown")).isEqualTo(KeyStoreConfig.KeyStoreType.JKS);
        assertThat(KeyStoreConfig.KeyStoreType.getTypeByName(null)).isEqualTo(KeyStoreConfig.KeyStoreType.JKS);

        KeyStoreConfig explicit =
                new KeyStoreConfig("PKCS12", new FileSystemResource(TestKeystores.path()), null, PASSWORD);
        assertThat(explicit.createKeyStore().size()).isEqualTo(3);
        assertThat(explicit.toString()).contains("PKCS12").contains("test.p12");

        KeyStoreConfig withProvider =
                new KeyStoreConfig("PKCS12", new FileSystemResource(TestKeystores.path()), "SunJSSE", PASSWORD);
        assertThat(withProvider.createKeyStore().size()).isEqualTo(3);

        KeyStoreConfig byExtension = config(TestKeystores.path());
        assertThat(byExtension.createKeyStore().size()).isEqualTo(3);

        assertThat(new KeyStoreConfig("", null, null, null).toString()).contains("nulledkeystore");
    }

    @Test
    @DisplayName("Монитор пишет WARN для скоро истекающих и ERROR для уже истёкших сертификатов")
    void monitorLogsByExpiry() {
        Logger logger = (Logger) LoggerFactory.getLogger(KeystoreExpirationMonitor.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            KeyStoreObserver observer = () -> List.of(
                    new ExpireCertData("soon", 10, "hash1", "file:a"),
                    new ExpireCertData("gone", -3, "hash2", "file:b"));
            new KeystoreExpirationMonitor(observer, 1, 120).check();

            assertThat(appender.list).extracting(ILoggingEvent::getLevel).containsExactly(Level.WARN, Level.ERROR);
            assertThat(appender.list.get(1).getFormattedMessage())
                    .contains("gone")
                    .contains("3 days ago");

            appender.list.clear();
            new KeystoreExpirationMonitor(List::of, 1, 120).check();
            assertThat(appender.list.get(0).getLevel()).isEqualTo(Level.INFO);

            appender.list.clear();
            new KeystoreExpirationMonitor(
                            () -> {
                                throw new IllegalStateException("boom");
                            },
                            1,
                            120)
                    .check();
            assertThat(appender.list.get(0).getLevel()).isEqualTo(Level.ERROR);
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    @DisplayName("Монитор запускается и останавливается; интервал должен быть не меньше часа")
    void monitorLifecycle() {
        KeystoreExpirationMonitor monitor = new KeystoreExpirationMonitor(List::of, 1, 120);
        monitor.start();
        monitor.stop();

        assertThatThrownBy(() -> new KeystoreExpirationMonitor(List::of, 0, 120))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Настройки по умолчанию: выключено, 120 и 30 дней, проверка раз в сутки")
    void propertiesDefaults() {
        KeystoreMonitoringProperties properties = new KeystoreMonitoringProperties();
        KeystoreMonitoringProperties.Keystore keystore = new KeystoreMonitoringProperties.Keystore();
        keystore.setLocation("classpath:x.p12");
        keystore.setPassword("p");
        keystore.setType("PKCS12");
        keystore.setProvider("SunJSSE");
        properties.getKeystores().add(keystore);

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getDaysToExpire()).isEqualTo(120);
        assertThat(properties.getDaysExpired()).isEqualTo(30);
        assertThat(properties.getCheckIntervalHours()).isEqualTo(24);
        assertThat(keystore.getLocation()).isEqualTo("classpath:x.p12");
        assertThat(keystore.getProvider()).isEqualTo("SunJSSE");
        assertThat(keystore.getType()).isEqualTo("PKCS12");
        assertThat(keystore.getPassword()).isEqualTo("p");
    }
}
