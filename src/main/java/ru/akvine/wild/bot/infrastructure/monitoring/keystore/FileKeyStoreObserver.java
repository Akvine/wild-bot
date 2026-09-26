package ru.akvine.wild.bot.infrastructure.monitoring.keystore;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileKeyStoreObserver extends AbstractKeystoreObserver implements KeyStoreObserver {
    private final List<KeyStoreConfig> keyStoreConfigs;

    public FileKeyStoreObserver(
            List<KeyStoreConfig> keyStoreConfigs, int daysToExpire, boolean includeExpired, int includeExpiredDays) {
        super(includeExpired, includeExpiredDays, daysToExpire);
        this.keyStoreConfigs = keyStoreConfigs;
    }

    /**
     * Достаёт из файловых ключниц сертификаты, которые скоро истекут или уже истекли, и складывает их в один список
     *
     * @return информация о сертификатах, у которых срок действия либо скоро истечёт, либо уже истёк
     */
    @Override
    public List<ExpireCertData> observe() {
        List<ExpireCertData> data = new ArrayList<>();
        for (KeyStoreConfig keyStoreConfig : this.keyStoreConfigs) {
            try {
                data.addAll(procCertData(keyStoreConfig));
            } catch (Exception e) {
                logger.warn("Error observe [{}]", keyStoreConfig, e);
            }
        }
        return data;
    }

    private List<ExpireCertData> procCertData(KeyStoreConfig keyStoreConfig)
            throws IOException, KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException,
                    CertificateException {
        List<ExpireCertData> data = new ArrayList<>();
        KeyStore keyStore = keyStoreConfig.createKeyStore();
        Enumeration<String> aliases = keyStore.aliases();

        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            Certificate certificate = keyStore.getCertificate(alias);
            if (!(certificate instanceof X509Certificate)) {
                logger.trace("the certificate with alias {} is not X509Certificate", alias);
                continue;
            }

            if (checkForCertificateSkipping(certificate)) {
                continue;
            }
            String location = keyStoreConfig.getKeystoreFile().getURI().toString();
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(certificate.getEncoded()));
            data.add(new ExpireCertData(alias, getDaysBetween(certificate), hash, location));
        }
        return data;
    }
}
