package ru.akvine.wild.bot.unit.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.infrastructure.monitoring.micrometer.IntegrationMetrics;

@DisplayName("Метрики обращений к внешним системам")
class IntegrationMetricsTest {
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final IntegrationMetrics metrics = new IntegrationMetrics(registry);

    /** Сервис интеграции в миниатюре: обычный метод, перегрузка через default-метод и метод, который падает */
    public interface Messenger {
        String send(String chatId, String text);

        default String send(String chatId) {
            return send(chatId, "default text");
        }

        void fail();
    }

    interface HiddenMessenger {
        void send();
    }

    static class RealMessenger implements Messenger {
        final List<String> sent = new ArrayList<>();
        final IntegrationException failure = new IntegrationException("messenger is down");

        @Override
        public String send(String chatId, String text) {
            sent.add(chatId + ":" + text);
            return "sent to " + chatId;
        }

        @Override
        public void fail() {
            throw failure;
        }

        @Override
        public String toString() {
            return "real messenger";
        }
    }

    private final RealMessenger real = new RealMessenger();
    private final Messenger metered = metrics.instrument("telegram", Messenger.class, real);

    private Timer timer(String operation, String outcome, String exception) {
        return registry.find(IntegrationMetrics.REQUESTS)
                .tags("system", "telegram", "operation", operation, "outcome", outcome, "exception", exception)
                .timer();
    }

    @Test
    @DisplayName("Вызов доходит до сервиса, результат возвращается, обращение посчитано")
    void callIsDelegatedAndCounted() {
        String result = metered.send("100", "hello");

        assertThat(result).isEqualTo("sent to 100");
        assertThat(real.sent).containsExactly("100:hello");
        assertThat(timer("send", "success", "none").count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Исключение сервиса приходит вызывающему коду как есть и считается ошибкой")
    void exceptionIsRethrownAsIsAndCountedAsError() {
        assertThatThrownBy(metered::fail).isSameAs(real.failure);

        assertThat(timer("fail", "error", "IntegrationException").count()).isEqualTo(1);
        assertThat(timer("fail", "success", "none")).isNull();
    }

    @Test
    @DisplayName("Перегрузка, которая внутри сервиса зовёт другой его метод, считается один раз")
    void nestedCallInsideServiceIsCountedOnce() {
        String result = metered.send("100");

        assertThat(result).isEqualTo("sent to 100");
        assertThat(real.sent).containsExactly("100:default text");
        assertThat(timer("send", "success", "none").count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Системы считаются раздельно")
    void systemsAreCountedSeparately() {
        Messenger max = metrics.instrument("max", Messenger.class, real);

        metered.send("100", "hello");
        max.send("200", "hello");

        assertThat(timer("send", "success", "none").count()).isEqualTo(1);
        assertThat(registry.find(IntegrationMetrics.REQUESTS)
                        .tags("system", "max", "operation", "send")
                        .timer()
                        .count())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Обернуть можно только публичный интерфейс: ошибка видна сразу, а не при первом вызове")
    void onlyPublicInterfaceCanBeInstrumented() {
        assertThatThrownBy(() -> metrics.instrument("telegram", HiddenMessenger.class, () -> {}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be a public interface");
        assertThatThrownBy(() -> metrics.instrument("telegram", RealMessenger.class, real))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Методы Object в метрики не попадают")
    void objectMethodsAreNotCounted() {
        assertThat(metered.toString()).isEqualTo("real messenger");
        assertThat(metered.hashCode()).isEqualTo(real.hashCode());

        assertThat(registry.find(IntegrationMetrics.REQUESTS).timers()).isEmpty();
    }
}
