package ru.akvine.wild.bot.unit.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.bot.filter.MessageFilter;
import ru.akvine.wild.bot.bot.filter.MetricsFilter;
import ru.akvine.wild.bot.enums.BotDataType;
import ru.akvine.wild.bot.enums.BotType;

@DisplayName("Метрики обработки обновлений бота")
class MetricsFilterTest {
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final MetricsFilter filter = new MetricsFilter(registry);

    private void next(Function<Payload, Response> handler) {
        filter.setNextMessageFilter(new MessageFilter() {
            @Override
            public Response handle(Payload payload) {
                return handler.apply(payload);
            }
        });
    }

    private Payload payload(BotType botType, BotDataType dataType) {
        return new Payload().setChatId("100").setBotType(botType).setBotDataType(dataType);
    }

    private Timer timer(String bot, String type, String outcome, String exception) {
        return registry.find(MetricsFilter.UPDATES)
                .tags("bot", bot, "type", type, "outcome", outcome, "exception", exception)
                .timer();
    }

    @Test
    @DisplayName("Обработанное обновление считается успешным, ответ фильтр не меняет")
    void processedUpdateIsSuccess() {
        Response answer = new Response("100", "answer", BotType.TELEGRAM);
        next(payload -> answer);

        Response response = filter.handle(payload(BotType.TELEGRAM, BotDataType.MESSAGE));
        filter.handle(payload(BotType.TELEGRAM, BotDataType.MESSAGE));

        assertThat(response).isSameAs(answer);
        assertThat(timer("telegram", "message", "success", "none").count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Боты и виды обновлений считаются раздельно")
    void botsAndTypesAreCountedSeparately() {
        next(payload -> new Response(payload.getChatId(), payload.getBotType()));

        filter.handle(payload(BotType.TELEGRAM, BotDataType.CALLBACK));
        filter.handle(payload(BotType.MAX, BotDataType.MESSAGE));

        assertThat(timer("telegram", "callback", "success", "none").count()).isEqualTo(1);
        assertThat(timer("max", "message", "success", "none").count()).isEqualTo(1);
        assertThat(timer("telegram", "message", "success", "none")).isNull();
    }

    @Test
    @DisplayName("Повторная доставка считается отдельно от обработанных обновлений")
    void ignoredUpdateIsDuplicate() {
        next(payload -> Response.ignored(payload.getChatId(), payload.getBotType()));

        Response response = filter.handle(payload(BotType.MAX, BotDataType.CALLBACK));

        assertThat(response.isIgnored()).isTrue();
        assertThat(timer("max", "callback", "duplicate", "none").count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Исключение считается ошибкой с именем своего класса и уходит дальше без изменений")
    void exceptionIsErrorAndIsRethrown() {
        IllegalStateException failure = new IllegalStateException("boom");
        next(payload -> {
            throw failure;
        });

        assertThatThrownBy(() -> filter.handle(payload(BotType.TELEGRAM, BotDataType.MESSAGE)))
                .isSameAs(failure);

        assertThat(timer("telegram", "message", "error", "IllegalStateException")
                        .count())
                .isEqualTo(1);
        assertThat(timer("telegram", "message", "success", "none")).isNull();
    }

    @Test
    @DisplayName("Обновление без типа бота и вида данных не ломает замер")
    void payloadWithoutTypesIsUnknown() {
        next(payload -> new Response());

        filter.handle(new Payload().setChatId("100"));

        assertThat(timer("unknown", "unknown", "success", "none").count()).isEqualTo(1);
    }
}
