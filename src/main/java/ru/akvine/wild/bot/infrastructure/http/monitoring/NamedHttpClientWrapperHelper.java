package ru.akvine.wild.bot.infrastructure.http.monitoring;

import java.util.Optional;
import java.util.function.Supplier;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

public final class NamedHttpClientWrapperHelper {
    private NamedHttpClientWrapperHelper() {}

    public static CloseableHttpClient wrap(CloseableHttpClient client, String name) {
        return new NamedCloseableHttpClientWrapper(client, name);
    }

    public static <T> T executeWithNameManually(String name, Supplier<T> action) {
        String previous = HttpClientNameHolder.getThreadLocalName();
        try {
            HttpClientNameHolder.setThreadLocalName(name);
            return action.get();
        } finally {
            restore(previous);
        }
    }

    public static void executeWithNameManually(String name, Runnable runnable) {
        executeWithNameManually(name, () -> {
            runnable.run();
            return null;
        });
    }

    public static Optional<String> getCurrentThreadLocalClientName() {
        return Optional.ofNullable(HttpClientNameHolder.getThreadLocalName());
    }

    static void restore(String previous) {
        if (previous == null) {
            HttpClientNameHolder.removeThreadLocalName();
        } else {
            HttpClientNameHolder.setThreadLocalName(previous);
        }
    }
}
