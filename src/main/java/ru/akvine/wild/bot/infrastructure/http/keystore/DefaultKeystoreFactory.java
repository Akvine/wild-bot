package ru.akvine.wild.bot.infrastructure.http.keystore;

import org.springframework.core.io.ResourceLoader;

public class DefaultKeystoreFactory extends KeystoreFactory<KeystoreBuilder> {

    public DefaultKeystoreFactory(String keystoreFilePath, String keystorePassword) {
        super(keystoreFilePath, keystorePassword);
    }

    public DefaultKeystoreFactory(String keystoreFilePath, String keystorePassword, ResourceLoader resourceLoader) {
        super(keystoreFilePath, keystorePassword, resourceLoader);
    }

    @Override
    public KeystoreBuilder createKeystoreBuilder() {
        return new KeystoreBuilder(localKeystore, localKeystorePassword);
    }
}
