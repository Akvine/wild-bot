package ru.akvine.wild.bot.infrastructure.http.keystore;

import java.util.Map;
import java.util.Set;

/**
 * Достаёт из ключницы клиентские (с приватным ключом) и доверенные сертификаты
 */
public interface KeystoreExtractor {
    Map<String, KeystoreMergeEntry> getAllClientCertificates(boolean silent);

    Map<String, KeystoreMergeEntry> getClientCertificates(boolean silent, Set<String> aliases);

    Map<String, KeystoreMergeEntry> getAllTrustCertificates(boolean silent);

    Map<String, KeystoreMergeEntry> getTrustCertificates(boolean silent, Set<String> aliases);
}
