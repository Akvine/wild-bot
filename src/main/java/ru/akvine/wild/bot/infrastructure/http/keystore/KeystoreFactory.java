package ru.akvine.wild.bot.infrastructure.http.keystore;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import ru.akvine.wild.bot.infrastructure.http.keystore.exception.KeystoreFactoryException;

/**
 * Загружает локальную ключницу из файла и создаёт билдер поверх неё
 */
@Slf4j
public abstract class KeystoreFactory<T> {

    protected final KeyStore localKeystore;
    protected final String localKeystorePassword;

    public KeystoreFactory(String keystoreFilePath, String keystorePassword) {
        this(keystoreFilePath, keystorePassword, null);
    }

    /**
     * @param keystoreFilePath путь к ключнице; при {@code resourceLoader == null} - в classpath, иначе любой
     *                         путь, понятный загрузчику ({@code file:...}, {@code classpath:...})
     */
    public KeystoreFactory(String keystoreFilePath, String keystorePassword, ResourceLoader resourceLoader) {
        logger.info("try to load keystore {}", keystoreFilePath);
        try (InputStream keystoreInputStream = resourceLoader != null
                ? resourceLoader.getResource(keystoreFilePath).getInputStream()
                : new ClassPathResource(keystoreFilePath).getInputStream()) {

            this.localKeystorePassword = keystorePassword;

            localKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
            localKeystore.load(keystoreInputStream, localKeystorePassword.toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException ex) {
            throw new KeystoreFactoryException("Can't load local keystore.", ex);
        }
    }

    public abstract T createKeystoreBuilder();
}
