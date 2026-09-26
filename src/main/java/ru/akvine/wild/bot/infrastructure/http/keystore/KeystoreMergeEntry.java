package ru.akvine.wild.bot.infrastructure.http.keystore;

import java.security.PrivateKey;
import java.security.cert.Certificate;

/**
 * Запись ключницы: сертификат и, для клиентских сертификатов, приватный ключ
 */
public class KeystoreMergeEntry {
    private final Certificate certificate;
    private final PrivateKey privateKey;

    public KeystoreMergeEntry(Certificate certificate, PrivateKey privateKey) {
        this.certificate = certificate;
        this.privateKey = privateKey;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}
