package ru.akvine.wild.bot.infrastructure.http;

import javax.net.ssl.HostnameVerifier;

public interface HostnameVerifierProvider {
    /**
     * Возвращает свой {@link HostnameVerifier} либо {@code null} (использовать стандартный)
     */
    HostnameVerifier getHostnameVerifier();
}
