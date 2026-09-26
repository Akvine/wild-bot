package ru.akvine.wild.bot.infrastructure.http.keystore;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import ru.akvine.wild.bot.infrastructure.http.keystore.exception.AliasDoesNotExistException;
import ru.akvine.wild.bot.infrastructure.http.keystore.exception.KeystoreConversionException;

/**
 * Достаёт сертификаты из уже загруженной локальной ключницы. Флаг {@code silent} превращает
 * исключения в предупреждения в логе, а проблемная запись пропускается
 */
public class LocalKeystoreExtractor implements KeystoreExtractor {
    private final KeyStore localKeystore;
    private final String localKeystorePassword;

    public LocalKeystoreExtractor(KeyStore localKeystore, String localKeystorePassword) {
        this.localKeystore = localKeystore;
        this.localKeystorePassword = localKeystorePassword;
    }

    /**
     * @param silent не выбрасывать {@link KeystoreConversionException}
     * @return alias -> запись
     * @throws IllegalStateException если ключница не инициализирована
     */
    @Override
    public Map<String, KeystoreMergeEntry> getAllClientCertificates(boolean silent) {
        try {
            Map<String, KeystoreMergeEntry> clientCertificateMap = new HashMap<>();
            Enumeration<String> aliases = localKeystore.aliases();

            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                new SilentUtils.ThrowOrWarner()
                        .exceptionMapper(KeystoreConversionException::new)
                        .silent(silent)
                        .messageTemplate(
                                "Cant't convert local client certificate with alias '%s'. "
                                        + "While including all local client certificates.",
                                alias)
                        .exceptionThrower(() -> putClientCertificate(clientCertificateMap, alias))
                        .throwOrWarn();
            }

            return clientCertificateMap;
        } catch (KeyStoreException ex) {
            throw new IllegalStateException("Local keystore doesn't initialized!", ex);
        }
    }

    /**
     * @param silent не выбрасывать {@link KeystoreConversionException} и {@link AliasDoesNotExistException}
     * @param aliases алиасы клиентских сертификатов
     * @return alias -> запись
     * @throws IllegalStateException если ключница не инициализирована
     */
    @Override
    public Map<String, KeystoreMergeEntry> getClientCertificates(boolean silent, Set<String> aliases) {
        Objects.requireNonNull(aliases, "aliases is null");
        try {
            Map<String, KeystoreMergeEntry> clientCertificateMap = new HashMap<>();
            for (String alias : aliases) {
                if (!localKeystore.containsAlias(alias)) {
                    new SilentUtils.ThrowOrWarner()
                            .exceptionMapper(AliasDoesNotExistException::new)
                            .silent(silent)
                            .messageTemplate("Local keystore doesn't contain client certificate with alias '%s'.", alias)
                            .throwOrWarn();
                    continue;
                }

                // без приватного ключа это не клиентский сертификат - для вызывающего это то же, что «нет такого алиаса»
                if (!localKeystore.isKeyEntry(alias)) {
                    new SilentUtils.ThrowOrWarner()
                            .exceptionMapper(AliasDoesNotExistException::new)
                            .silent(silent)
                            .messageTemplate(
                                    "Local keystore doesn't contain client certificate with alias '%s'. "
                                            + "Trust certificate found.",
                                    alias)
                            .throwOrWarn();
                    continue;
                }

                new SilentUtils.ThrowOrWarner()
                        .exceptionMapper(KeystoreConversionException::new)
                        .silent(silent)
                        .messageTemplate(
                                "Cant't convert local client certificate with alias '%s'. "
                                        + "While including local client certificates aliases.",
                                alias)
                        .exceptionThrower(() -> putClientCertificate(clientCertificateMap, alias))
                        .throwOrWarn();
            }

            return clientCertificateMap;
        } catch (KeyStoreException ex) {
            throw new IllegalStateException("Local keystore doesn't initialized!", ex);
        }
    }

    /**
     * @param silent не используется, оставлен для единообразия интерфейса
     * @return alias -> запись
     * @throws IllegalStateException если ключница не инициализирована
     */
    @Override
    public Map<String, KeystoreMergeEntry> getAllTrustCertificates(boolean silent) {
        try {
            Map<String, KeystoreMergeEntry> trustCertificateMap = new HashMap<>();
            Enumeration<String> aliases = localKeystore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (localKeystore.isCertificateEntry(alias)) {
                    trustCertificateMap.put(alias, new KeystoreMergeEntry(localKeystore.getCertificate(alias), null));
                }
            }

            return trustCertificateMap;
        } catch (KeyStoreException ex) {
            throw new IllegalStateException("Local keystore doesn't initialized!", ex);
        }
    }

    /**
     * @param silent не выбрасывать {@link AliasDoesNotExistException}
     * @param aliases алиасы доверенных сертификатов
     * @return alias -> запись
     * @throws IllegalStateException если ключница не инициализирована
     */
    @Override
    public Map<String, KeystoreMergeEntry> getTrustCertificates(boolean silent, Set<String> aliases) {
        Objects.requireNonNull(aliases, "aliases is null");
        try {
            Map<String, KeystoreMergeEntry> trustCertificateMap = new HashMap<>();
            for (String alias : aliases) {
                if (!localKeystore.containsAlias(alias)) {
                    new SilentUtils.ThrowOrWarner()
                            .exceptionMapper(AliasDoesNotExistException::new)
                            .silent(silent)
                            .messageTemplate("Local keystore doesn't contain trust certificate with alias '%s'.", alias)
                            .throwOrWarn();
                    continue;
                }

                // с приватным ключом это клиентский сертификат, а не доверенный
                if (!localKeystore.isCertificateEntry(alias)) {
                    new SilentUtils.ThrowOrWarner()
                            .exceptionMapper(AliasDoesNotExistException::new)
                            .silent(silent)
                            .messageTemplate(
                                    "Local keystore doesn't contain trust certificate with alias '%s'. "
                                            + "Client certificate found.",
                                    alias)
                            .throwOrWarn();
                    continue;
                }

                trustCertificateMap.put(alias, new KeystoreMergeEntry(localKeystore.getCertificate(alias), null));
            }

            return trustCertificateMap;
        } catch (KeyStoreException ex) {
            throw new IllegalStateException("Local keystore doesn't initialized!", ex);
        }
    }

    private void putClientCertificate(Map<String, KeystoreMergeEntry> clientCertificateMap, String alias)
            throws Exception {
        if (localKeystore.isKeyEntry(alias)) {
            Certificate certificate = localKeystore.getCertificate(alias);
            PrivateKey privateKey = (PrivateKey) localKeystore.getKey(alias, localKeystorePassword.toCharArray());
            clientCertificateMap.put(alias, new KeystoreMergeEntry(certificate, privateKey));
        }
    }
}
