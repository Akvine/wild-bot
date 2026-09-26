package ru.akvine.wild.bot.infrastructure.http.keystore;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;
import ru.akvine.wild.bot.infrastructure.http.keystore.exception.KeystoreBuilderException;

/**
 * Собирает {@link KeyStore} из выбранных сертификатов локальной ключницы
 */
public class KeystoreBuilder extends AbstractKeystoreBuilder<KeystoreBuilder, KeyStore> {

    protected KeystoreBuilder(KeyStore localKeystore, String localKeystorePassword) {
        super(localKeystore, localKeystorePassword);
    }

    @Override
    protected KeyStore tryBuild(
            String keystoreType,
            Map<String, KeystoreMergeEntry> mergeClientCertificateMap,
            Map<String, KeystoreMergeEntry> mergeTrustCertificateMap) {
        try {
            KeyStore keyStore = KeyStore.getInstance(keystoreType);
            keyStore.load(null, keystorePassword().toCharArray());

            for (Map.Entry<String, KeystoreMergeEntry> clientEntry : mergeClientCertificateMap.entrySet()) {
                throwOrWarnSetKeyEntry(clientEntry, keyStore);
            }

            for (Map.Entry<String, KeystoreMergeEntry> trustEntry : mergeTrustCertificateMap.entrySet()) {
                throwOrWarnSetCertificateEntry(trustEntry, keyStore);
            }

            return keyStore;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new KeystoreBuilderException("Can't create keystore", e);
        }
    }
}
