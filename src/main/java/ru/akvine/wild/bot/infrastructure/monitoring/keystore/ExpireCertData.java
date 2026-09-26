package ru.akvine.wild.bot.infrastructure.monitoring.keystore;

/**
 * Сертификат, у которого срок действия скоро истекает или уже истёк
 *
 * @param alias алиас в ключнице
 * @param expiryIn дней до окончания срока действия (отрицательное - уже истёк)
 * @param hash sha256 сертификата
 * @param source откуда взята ключница
 */
public record ExpireCertData(String alias, int expiryIn, String hash, String source) {}
