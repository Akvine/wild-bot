package ru.akvine.wild.bot.infrastructure.http.keystore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.SSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResourceLoader;
import ru.akvine.wild.bot.infrastructure.http.keystore.exception.AliasDoesNotExistException;
import ru.akvine.wild.bot.infrastructure.http.keystore.exception.KeystoreBuilderException;
import ru.akvine.wild.bot.infrastructure.http.keystore.exception.KeystoreConversionException;
import ru.akvine.wild.bot.infrastructure.http.keystore.exception.KeystoreEmptyException;
import ru.akvine.wild.bot.infrastructure.http.keystore.exception.KeystoreFactoryException;
import ru.akvine.wild.bot.infrastructure.http.keystore.exception.KeystoreMergeException;
import ru.akvine.wild.bot.testsupport.TestKeystores;

@DisplayName("Ключницы: извлечение, сборка, SSL-контекст")
class KeystoreTest {
    private static final String PASSWORD = TestKeystores.PASSWORD;

    private KeyStore keyStore;
    private LocalKeystoreExtractor extractor;

    @BeforeEach
    void setUp() throws Exception {
        keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (var in = Files.newInputStream(TestKeystores.path())) {
            keyStore.load(in, PASSWORD.toCharArray());
        }
        extractor = new LocalKeystoreExtractor(keyStore, PASSWORD);
    }

    @Test
    @DisplayName("Все клиентские сертификаты: с приватным ключом, без доверенных")
    void allClientCertificates() {
        Map<String, KeystoreMergeEntry> result = extractor.getAllClientCertificates(false);

        assertThat(result).containsOnlyKeys(TestKeystores.CLIENT_1, TestKeystores.CLIENT_2);
        assertThat(result.get(TestKeystores.CLIENT_1).getPrivateKey()).isNotNull();
        assertThat(result.get(TestKeystores.CLIENT_1).getCertificate()).isNotNull();
    }

    @Test
    @DisplayName("Клиентские сертификаты по алиасам: неизвестный и доверенный алиас - ошибка или предупреждение")
    void clientCertificatesByAlias() {
        assertThat(extractor.getClientCertificates(false, Set.of(TestKeystores.CLIENT_1)))
                .containsOnlyKeys(TestKeystores.CLIENT_1);

        assertThatThrownBy(() -> extractor.getClientCertificates(false, Set.of("missing")))
                .isInstanceOf(AliasDoesNotExistException.class);
        assertThatThrownBy(() -> extractor.getClientCertificates(false, Set.of(TestKeystores.TRUST_1)))
                .isInstanceOf(AliasDoesNotExistException.class);

        assertThat(extractor.getClientCertificates(true, Set.of("missing", TestKeystores.TRUST_1)))
                .isEmpty();
        assertThatThrownBy(() -> extractor.getClientCertificates(false, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Неверный пароль ключа: ошибка конвертации или пропуск записи в тихом режиме")
    void wrongKeyPassword() {
        LocalKeystoreExtractor wrong = new LocalKeystoreExtractor(keyStore, "wrong-password");

        assertThatThrownBy(() -> wrong.getAllClientCertificates(false)).isInstanceOf(KeystoreConversionException.class);
        assertThat(wrong.getAllClientCertificates(true)).isEmpty();
        assertThatThrownBy(() -> wrong.getClientCertificates(false, Set.of(TestKeystores.CLIENT_1)))
                .isInstanceOf(KeystoreConversionException.class);
        assertThat(wrong.getClientCertificates(true, Set.of(TestKeystores.CLIENT_1)))
                .isEmpty();
    }

    @Test
    @DisplayName("Доверенные сертификаты: все, по алиасам, клиентский алиас - ошибка")
    void trustCertificates() {
        assertThat(extractor.getAllTrustCertificates(false)).containsOnlyKeys(TestKeystores.TRUST_1);
        assertThat(extractor.getTrustCertificates(false, Set.of(TestKeystores.TRUST_1)))
                .containsOnlyKeys(TestKeystores.TRUST_1);
        assertThat(extractor
                        .getAllTrustCertificates(false)
                        .get(TestKeystores.TRUST_1)
                        .getPrivateKey())
                .isNull();

        assertThatThrownBy(() -> extractor.getTrustCertificates(false, Set.of("missing")))
                .isInstanceOf(AliasDoesNotExistException.class);
        assertThatThrownBy(() -> extractor.getTrustCertificates(false, Set.of(TestKeystores.CLIENT_1)))
                .isInstanceOf(AliasDoesNotExistException.class);
        assertThat(extractor.getTrustCertificates(true, Set.of("missing", TestKeystores.CLIENT_1)))
                .isEmpty();
        assertThatThrownBy(() -> extractor.getTrustCertificates(false, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Неинициализированная ключница: IllegalStateException")
    void uninitializedKeystore() throws Exception {
        LocalKeystoreExtractor broken =
                new LocalKeystoreExtractor(KeyStore.getInstance(KeyStore.getDefaultType()), PASSWORD);

        assertThatThrownBy(() -> broken.getAllClientCertificates(false)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> broken.getClientCertificates(false, Set.of("a")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> broken.getAllTrustCertificates(false)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> broken.getTrustCertificates(false, Set.of("a")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("KeystoreBuilder собирает ключницу из выбранных алиасов")
    void builderBuildsKeystore() throws Exception {
        KeyStore built = new KeystoreBuilder(keyStore, PASSWORD)
                .withLocalClientCertificate(TestKeystores.CLIENT_1)
                .withLocalClientCertificate(Set.of(TestKeystores.CLIENT_2))
                .withLocalTrustCertificate(TestKeystores.TRUST_1)
                .withKeystorePassword("another")
                .build();

        assertThat(Collections.list(built.aliases()))
                .containsExactlyInAnyOrder(TestKeystores.CLIENT_1, TestKeystores.CLIENT_2, TestKeystores.TRUST_1);
        assertThat(built.isKeyEntry(TestKeystores.CLIENT_1)).isTrue();
        assertThat(built.isCertificateEntry(TestKeystores.TRUST_1)).isTrue();
    }

    @Test
    @DisplayName("KeystoreBuilder: includeAll* и перегрузки withLocalTrustCertificate")
    void builderIncludeAll() throws Exception {
        KeyStore built = new KeystoreBuilder(keyStore, PASSWORD)
                .includeAllLocalClientCertificates()
                .includeAllLocalTrustCertificates()
                .build();
        assertThat(Collections.list(built.aliases())).hasSize(3);

        KeyStore trustOnly = new KeystoreBuilder(keyStore, PASSWORD)
                .withLocalTrustCertificate(List.of(TestKeystores.TRUST_1))
                .withLocalTrustCertificate(Set.of(TestKeystores.TRUST_1))
                .build();
        assertThat(Collections.list(trustOnly.aliases())).containsExactly(TestKeystores.TRUST_1);
    }

    @Test
    @DisplayName("Пустой результат: ошибка, а в тихом режиме - только предупреждение")
    void emptyResult() throws Exception {
        assertThatThrownBy(() -> new KeystoreBuilder(keyStore, PASSWORD).build())
                .isInstanceOf(KeystoreEmptyException.class);

        KeyStore silent =
                new KeystoreBuilder(keyStore, PASSWORD).buildSilently().build();
        assertThat(silent.size()).isZero();
    }

    @Test
    @DisplayName("Неизвестный алиас в тихом режиме не мешает сборке")
    void silentBuildSkipsMissingAlias() throws Exception {
        KeyStore built = new KeystoreBuilder(keyStore, PASSWORD)
                .withLocalClientCertificate("missing", TestKeystores.CLIENT_1)
                .buildSilently()
                .build();

        assertThat(Collections.list(built.aliases())).containsExactly(TestKeystores.CLIENT_1);
    }

    @Test
    @DisplayName("Одинаковый алиас у клиентского и доверенного сертификата: ошибка, а тихо - приоритет у клиентского")
    void duplicatedAlias() throws Exception {
        Map<String, KeystoreMergeEntry> clients = new HashMap<>();
        clients.put("same", new KeystoreMergeEntry(keyStore.getCertificate(TestKeystores.CLIENT_1), null));
        Map<String, KeystoreMergeEntry> trusts = new HashMap<>();
        trusts.put("same", new KeystoreMergeEntry(keyStore.getCertificate(TestKeystores.TRUST_1), null));

        DuplicatingBuilder strict = new DuplicatingBuilder(clients, trusts);
        assertThatThrownBy(strict::build).isInstanceOf(KeystoreMergeException.class);

        trusts.put("same", new KeystoreMergeEntry(keyStore.getCertificate(TestKeystores.TRUST_1), null));
        DuplicatingBuilder silent = new DuplicatingBuilder(clients, trusts);
        silent.buildSilently();
        assertThat(silent.build()).containsExactly("same");
    }

    @Test
    @DisplayName("Ошибка записи в ключницу: строго - исключение, тихо - предупреждение")
    void setEntryFailures() throws Exception {
        Map<String, KeystoreMergeEntry> brokenEntries =
                Map.of("bad", new KeystoreMergeEntry(keyStore.getCertificate(TestKeystores.CLIENT_1), null));
        KeyStore uninitialized = KeyStore.getInstance(KeyStore.getDefaultType());

        ExposingBuilder strict = new ExposingBuilder();
        for (Map.Entry<String, KeystoreMergeEntry> entry : brokenEntries.entrySet()) {
            assertThatThrownBy(() -> strict.setKey(entry, uninitialized)).isInstanceOf(KeystoreBuilderException.class);
            assertThatThrownBy(() -> strict.setCertificate(entry, uninitialized))
                    .isInstanceOf(KeystoreBuilderException.class);
        }

        ExposingBuilder silent = new ExposingBuilder();
        silent.buildSilently();
        for (Map.Entry<String, KeystoreMergeEntry> entry : brokenEntries.entrySet()) {
            silent.setKey(entry, uninitialized);
            silent.setCertificate(entry, uninitialized);
        }
    }

    @Test
    @DisplayName("Ключница превращается в SSL-контекст; неверный пароль - KeystoreConversionException")
    void sslContext() {
        SSLContext context = SslContextUtils.keyStoreToSslContext(keyStore, PASSWORD);
        assertThat(context.getProtocol()).isEqualTo("TLS");

        assertThatThrownBy(() -> SslContextUtils.keyStoreToSslContext(keyStore, "wrong"))
                .isInstanceOf(KeystoreConversionException.class);
        assertThatThrownBy(() -> SslContextUtils.keyStoreToSslContext(null, PASSWORD))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> SslContextUtils.keyStoreToSslContext(keyStore, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Фабрика грузит ключницу из файла и создаёт билдер; ошибки загрузки - KeystoreFactoryException")
    void factory() throws IOException {
        String path = TestKeystores.path().toUri().toString();

        DefaultKeystoreFactory factory = new DefaultKeystoreFactory(path, PASSWORD, new FileSystemResourceLoader());
        assertThat(factory.createKeystoreBuilder()).isNotNull();

        assertThatThrownBy(() -> new DefaultKeystoreFactory("no/such.p12", PASSWORD))
                .isInstanceOf(KeystoreFactoryException.class);
        assertThatThrownBy(() -> new DefaultKeystoreFactory(path, "wrong-password", new FileSystemResourceLoader()))
                .isInstanceOf(KeystoreFactoryException.class);

        Path broken = Files.createTempFile("broken", ".p12");
        Files.writeString(broken, "not a keystore");
        assertThatThrownBy(() ->
                        new DefaultKeystoreFactory(broken.toUri().toString(), PASSWORD, new FileSystemResourceLoader()))
                .isInstanceOf(KeystoreFactoryException.class);
    }

    @Test
    @DisplayName("ThrowOrWarner: с исполнителем и без, строго и тихо")
    void throwOrWarner() {
        assertThatThrownBy(() -> new SilentUtils.ThrowOrWarner()
                        .exceptionMapper(IllegalStateException::new)
                        .messageTemplate("failed %s", "x")
                        .throwOrWarn())
                .hasMessage("failed x");
        new SilentUtils.ThrowOrWarner()
                .exceptionMapper(IllegalStateException::new)
                .silent(true)
                .messageTemplate("failed")
                .throwOrWarn();

        assertThatThrownBy(() -> new SilentUtils.ThrowOrWarner()
                        .exceptionMapper(IllegalStateException::new)
                        .messageTemplate("wrapped")
                        .exceptionThrower(() -> {
                            throw new Exception("inner");
                        })
                        .throwOrWarn())
                .isInstanceOf(IllegalStateException.class)
                .hasCauseInstanceOf(Exception.class);
        new SilentUtils.ThrowOrWarner()
                .exceptionMapper(IllegalStateException::new)
                .silent(true)
                .messageTemplate("wrapped")
                .exceptionThrower(() -> {
                    throw new Exception("inner");
                })
                .throwOrWarn();
        new SilentUtils.ThrowOrWarner()
                .exceptionMapper(IllegalStateException::new)
                .messageTemplate("fine")
                .exceptionThrower(() -> {})
                .throwOrWarn();

        assertThatThrownBy(() ->
                        new SilentUtils.ThrowOrWarner().messageTemplate("m").throwOrWarn())
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new SilentUtils.ThrowOrWarner()
                        .exceptionMapper(IllegalStateException::new)
                        .throwOrWarn())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Исключения ключницы хранят сообщение и причину")
    void exceptions() {
        Throwable cause = new RuntimeException("cause");
        assertThat(new AliasDoesNotExistException("m")).hasMessage("m");
        assertThat(new KeystoreBuilderException("m")).hasMessage("m");
        assertThat(new KeystoreConversionException("m")).hasMessage("m");
        assertThat(new KeystoreEmptyException("m", cause)).hasCause(cause);
        assertThat(new KeystoreFactoryException("m")).hasMessage("m");
        assertThat(new KeystoreMergeException("m", cause)).hasCause(cause);
    }

    /** Билдер, у которого «прочитанные» сертификаты заданы напрямую */
    private class DuplicatingBuilder extends AbstractKeystoreMergeBuilder<DuplicatingBuilder, Set<String>> {
        private final Map<String, KeystoreMergeEntry> clients;
        private final Map<String, KeystoreMergeEntry> trusts;

        DuplicatingBuilder(Map<String, KeystoreMergeEntry> clients, Map<String, KeystoreMergeEntry> trusts) {
            super(keyStore, PASSWORD);
            this.clients = new HashMap<>(clients);
            this.trusts = new HashMap<>(trusts);
        }

        @Override
        protected Map<String, KeystoreMergeEntry> readLocalClientCertificates() {
            return clients;
        }

        @Override
        protected Map<String, KeystoreMergeEntry> readLocalTrustCertificates() {
            return trusts;
        }

        @Override
        protected Set<String> internalBuild(
                Map<String, KeystoreMergeEntry> mergeClientCertificateMap,
                Map<String, KeystoreMergeEntry> mergeTrustCertificateMap) {
            return Set.copyOf(mergeClientCertificateMap.keySet());
        }
    }

    private class ExposingBuilder extends AbstractKeystoreBuilder<ExposingBuilder, Certificate> {
        ExposingBuilder() {
            super(keyStore, PASSWORD);
        }

        void setKey(Map.Entry<String, KeystoreMergeEntry> entry, KeyStore target) {
            throwOrWarnSetKeyEntry(entry, target);
        }

        void setCertificate(Map.Entry<String, KeystoreMergeEntry> entry, KeyStore target) {
            throwOrWarnSetCertificateEntry(entry, target);
        }

        @Override
        protected Certificate tryBuild(
                String keystoreType,
                Map<String, KeystoreMergeEntry> mergeClientCertificateMap,
                Map<String, KeystoreMergeEntry> mergeTrustCertificateMap) {
            return null;
        }
    }
}
