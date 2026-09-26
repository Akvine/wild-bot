package ru.akvine.wild.bot.infrastructure.http.keystore;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Objects;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import ru.akvine.wild.bot.infrastructure.http.keystore.exception.KeystoreConversionException;

public final class SslContextUtils {

    private SslContextUtils() {}

    /**
     * Ключница служит одновременно источником клиентских сертификатов (key manager)
     * и доверенных сертификатов сервера (trust manager)
     */
    public static SSLContext keyStoreToSslContext(KeyStore keyStore, String password) {
        Objects.requireNonNull(keyStore, "keyStore is null");
        Objects.requireNonNull(password, "password is null");

        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, password.toCharArray());

            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());

            return sslContext;
        } catch (Exception ex) {
            throw new KeystoreConversionException("Can't convert keystore to ssl context.", ex);
        }
    }
}
