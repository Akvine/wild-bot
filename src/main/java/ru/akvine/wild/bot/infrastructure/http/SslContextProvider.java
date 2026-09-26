package ru.akvine.wild.bot.infrastructure.http;

import javax.net.ssl.SSLContext;

public interface SslContextProvider {
    SSLContext getSslContext();
}
