package ru.akvine.wild.bot.infrastructure.http.keystore;

import java.security.KeyStore;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import ru.akvine.wild.bot.infrastructure.http.keystore.exception.KeystoreEmptyException;
import ru.akvine.wild.bot.infrastructure.http.keystore.exception.KeystoreMergeException;

/**
 * Собирает результат из выбранных клиентских и доверенных сертификатов локальной ключницы
 *
 * @param <T> конкретный билдер, для цепочки вызовов
 * @param <R> что получается в итоге
 */
@Slf4j
public abstract class AbstractKeystoreMergeBuilder<T extends AbstractKeystoreMergeBuilder<T, R>, R> {

    private final Set<String> localClientCertificateAliases = new HashSet<>();
    private final Set<String> localTrustCertificateAliases = new HashSet<>();

    private final KeystoreExtractor localKeystoreExtractor;

    private boolean includeAllLocalClientCertificates = false;
    private boolean includeAllLocalTrustCertificates = false;

    protected boolean buildSilently = false;

    protected AbstractKeystoreMergeBuilder(KeyStore localKeystore, String localKeystorePassword) {
        this.localKeystoreExtractor = new LocalKeystoreExtractor(localKeystore, localKeystorePassword);
    }

    /** клиентские сертификаты (с приватным ключом) по алиасам */
    @SuppressWarnings("unchecked")
    public final T withLocalClientCertificate(String... aliases) {
        Objects.requireNonNull(aliases, "aliases is null");
        localClientCertificateAliases.addAll(Arrays.asList(aliases));
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public final T withLocalClientCertificate(Set<String> aliases) {
        Objects.requireNonNull(aliases, "aliases is null");
        localClientCertificateAliases.addAll(aliases);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public final T includeAllLocalClientCertificates() {
        this.includeAllLocalClientCertificates = true;
        return (T) this;
    }

    /** доверенные сертификаты по алиасам */
    @SuppressWarnings("unchecked")
    public final T withLocalTrustCertificate(String... aliases) {
        Objects.requireNonNull(aliases, "aliases is null");
        localTrustCertificateAliases.addAll(Arrays.asList(aliases));
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public final T withLocalTrustCertificate(List<String> aliases) {
        Objects.requireNonNull(aliases, "aliases is null");
        localTrustCertificateAliases.addAll(aliases);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public final T withLocalTrustCertificate(Set<String> aliases) {
        Objects.requireNonNull(aliases, "aliases is null");
        localTrustCertificateAliases.addAll(aliases);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public final T includeAllLocalTrustCertificates() {
        this.includeAllLocalTrustCertificates = true;
        return (T) this;
    }

    /** ошибки чтения записей и дубликаты алиасов превращаются в предупреждения в логе */
    @SuppressWarnings("unchecked")
    public final T buildSilently() {
        this.buildSilently = true;
        return (T) this;
    }

    public R build() {
        Map<String, KeystoreMergeEntry> mergeClientCertificateMap = new HashMap<>(readLocalClientCertificates());
        Map<String, KeystoreMergeEntry> mergeTrustCertificateMap = new HashMap<>(readLocalTrustCertificates());

        validateDuplicates(mergeClientCertificateMap, mergeTrustCertificateMap);

        return internalBuild(mergeClientCertificateMap, mergeTrustCertificateMap);
    }

    protected Map<String, KeystoreMergeEntry> readLocalClientCertificates() {
        if (includeAllLocalClientCertificates) {
            return localKeystoreExtractor.getAllClientCertificates(buildSilently);
        } else if (!localClientCertificateAliases.isEmpty()) {
            return localKeystoreExtractor.getClientCertificates(buildSilently, localClientCertificateAliases);
        }

        return Collections.emptyMap();
    }

    protected Map<String, KeystoreMergeEntry> readLocalTrustCertificates() {
        if (includeAllLocalTrustCertificates) {
            return localKeystoreExtractor.getAllTrustCertificates(buildSilently);
        } else if (!localTrustCertificateAliases.isEmpty()) {
            return localKeystoreExtractor.getTrustCertificates(buildSilently, localTrustCertificateAliases);
        }

        return Collections.emptyMap();
    }

    protected void validateDuplicates(
            Map<String, KeystoreMergeEntry> mergeClientCertificateMap,
            Map<String, KeystoreMergeEntry> mergeTrustCertificateMap) {
        for (String clientCertificateAlias : mergeClientCertificateMap.keySet()) {
            if (mergeTrustCertificateMap.get(clientCertificateAlias) != null) {
                if (buildSilently) {
                    logger.warn(
                            "Found duplicated alias for client and trust certificate. alias=\"{}\". "
                                    + "Trust certificate is removed, client certificate has priority.",
                            clientCertificateAlias);
                    mergeTrustCertificateMap.remove(clientCertificateAlias);
                } else {
                    throw new KeystoreMergeException("Found duplicated alias for client and trust certificate. alias=\""
                            + clientCertificateAlias + "\".");
                }
            }
        }

        if (mergeClientCertificateMap.isEmpty() && mergeTrustCertificateMap.isEmpty()) {
            if (buildSilently) {
                logger.warn("Merge finished, result keystore will be empty.");
            } else {
                throw new KeystoreEmptyException("Merge finished, result keystore will be empty.");
            }
        }
    }

    protected abstract R internalBuild(
            Map<String, KeystoreMergeEntry> mergeClientCertificateMap,
            Map<String, KeystoreMergeEntry> mergeTrustCertificateMap);
}
