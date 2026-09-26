package ru.akvine.wild.bot.infrastructure.http.keystore;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import ru.akvine.wild.bot.infrastructure.http.keystore.exception.KeystoreBuilderException;

/**
 * Собирает новую ключницу. Пароль ключей по умолчанию равен паролю исходной ключницы
 */
@Slf4j
public abstract class AbstractKeystoreBuilder<T extends AbstractKeystoreMergeBuilder<T, R>, R>
        extends AbstractKeystoreMergeBuilder<T, R> {

    private String keystorePassword;

    protected AbstractKeystoreBuilder(KeyStore localKeystore, String localKeystorePassword) {
        super(localKeystore, localKeystorePassword);
        this.keystorePassword = localKeystorePassword;
    }

    @SuppressWarnings("unchecked")
    public final T withKeystorePassword(String keystorePassword) {
        this.keystorePassword = Objects.requireNonNull(keystorePassword, "keystorePassword is null");
        return (T) this;
    }

    protected String keystorePassword() {
        return keystorePassword;
    }

    @Override
    public R internalBuild(
            Map<String, KeystoreMergeEntry> mergeClientCertificateMap,
            Map<String, KeystoreMergeEntry> mergeTrustCertificateMap) {
        return tryBuild(KeyStore.getDefaultType(), mergeClientCertificateMap, mergeTrustCertificateMap);
    }

    protected abstract R tryBuild(
            String keystoreType,
            Map<String, KeystoreMergeEntry> mergeClientCertificateMap,
            Map<String, KeystoreMergeEntry> mergeTrustCertificateMap);

    protected void throwOrWarnSetKeyEntry(Map.Entry<String, KeystoreMergeEntry> entry, KeyStore keyStore) {
        try {
            keyStore.setKeyEntry(
                    entry.getKey(),
                    entry.getValue().getPrivateKey(),
                    keystorePassword.toCharArray(),
                    new Certificate[] {entry.getValue().getCertificate()});
        } catch (KeyStoreException ex) {
            logger.warn("Can't set key entry.", ex);
            if (!buildSilently) {
                throw new KeystoreBuilderException("Can't set key entry.", ex);
            }
        }
    }

    protected void throwOrWarnSetCertificateEntry(Map.Entry<String, KeystoreMergeEntry> entry, KeyStore keyStore) {
        try {
            keyStore.setCertificateEntry(entry.getKey(), entry.getValue().getCertificate());
        } catch (KeyStoreException ex) {
            logger.warn("Can't set certificate entry.", ex);
            if (!buildSilently) {
                throw new KeystoreBuilderException("Can't set certificate entry.", ex);
            }
        }
    }
}
