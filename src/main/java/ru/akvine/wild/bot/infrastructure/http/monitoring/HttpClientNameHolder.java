package ru.akvine.wild.bot.infrastructure.http.monitoring;

/**
 * Имя http-клиента, который сейчас выполняет запрос в этом потоке. Доступно коду внутри самого
 * клиента (например, retry-стратегии), где иначе не видно, какой клиент работает
 */
class HttpClientNameHolder {
    private static final ThreadLocal<String> threadLocalName = new ThreadLocal<>();

    private HttpClientNameHolder() {}

    static void setThreadLocalName(String value) {
        threadLocalName.set(value);
    }

    static void removeThreadLocalName() {
        threadLocalName.remove();
    }

    static String getThreadLocalName() {
        return threadLocalName.get();
    }
}
