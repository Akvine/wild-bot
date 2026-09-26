package ru.akvine.wild.bot.infrastructure.http.monitoring;

public interface NamedHttpClient {
    /**
     * @return имя клиента, выполняющего запрос в текущем потоке, или {@code null}, если запрос не выполняется
     */
    String getHttpClientName();
}
