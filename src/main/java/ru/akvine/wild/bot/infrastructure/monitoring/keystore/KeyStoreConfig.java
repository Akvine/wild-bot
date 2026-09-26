package ru.akvine.wild.bot.infrastructure.monitoring.keystore;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.Objects;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Описание файловой ключницы: где лежит, какого типа и с каким паролем открывается
 */
public class KeyStoreConfig {
    private final KeyStoreType type;
    private final Resource keystore;
    private final String provider;
    private final String password;

    public KeyStoreConfig(String type, Resource keystore, String provider, String password) {
        this.type = StringUtils.hasText(type) ? KeyStoreType.getTypeByName(type) : null;
        this.keystore = keystore;
        this.provider = provider;
        this.password = password;
    }

    /**
     * Тип ключницы будет определён по расширению файла, что удаётся не всегда
     */
    public KeyStoreConfig(Resource keystore, String password) {
        this(null, keystore, null, password);
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return dotIndex > 0 ? fileName.substring(dotIndex + 1) : null;
    }

    public KeyStore createKeyStore()
            throws NoSuchProviderException, NoSuchAlgorithmException, KeyStoreException, IOException,
                    CertificateException {
        String keyStoreType = type != null
                ? type.name()
                : KeyStoreType.getTypeByName(getFileExtension(Objects.requireNonNull(keystore.getFilename())))
                        .name();
        KeyStore keyStore = StringUtils.hasText(provider)
                ? KeyStore.getInstance(keyStoreType, provider)
                : KeyStore.getInstance(keyStoreType);
        try (InputStream is = keystore.getInputStream()) {
            keyStore.load(is, StringUtils.hasText(password) ? password.toCharArray() : null);
        }
        return keyStore;
    }

    Resource getKeystoreFile() {
        return keystore;
    }

    @Override
    public String toString() {
        return "KeyStoreConfig{type=" + type + ", keystore='"
                + (keystore == null ? "nulledkeystore" : keystore.getFilename()) + "', provider='" + provider + "'}";
    }

    public enum KeyStoreType {
        JKS,
        JCEKS,
        PKCS12;

        /**
         * {@code pfx} - тот же PKCS12
         */
        public static KeyStoreType getTypeByName(String name) {
            if (name == null) {
                return JKS;
            }
            if (name.equalsIgnoreCase("pfx") || name.equalsIgnoreCase("p12")) {
                return PKCS12;
            }
            try {
                return valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return JKS;
            }
        }
    }
}
