package ru.akvine.wild.bot.unit.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.bot.filter.InitMessageFilter;
import ru.akvine.wild.bot.bot.filter.MessageFilter;
import ru.akvine.wild.bot.config.BotFilterConfig;
import ru.akvine.wild.bot.facades.MessageFiltersFacade;

@DisplayName("Место фильтра метрик в цепочке фильтров бота")
class BotFilterConfigMetricsTest {
    private static final String EXCEPTION_FILTER = "BotExceptionFilter";
    private static final String METRICS_FILTER = "MetricsFilter";
    private static final String CLIENT_FILTER = "ClientFilter";
    private static final String HANDLER_FILTER = "MessageHandlerFilter";

    private final List<String> visited = new ArrayList<>();
    private final BotFilterConfig config = new BotFilterConfig();

    /** Фильтры-заглушки: каждый записывает своё имя и передаёт сообщение дальше, так виден порядок цепочки */
    private MessageFiltersFacade facade() {
        Map<String, MessageFilter> filters = new HashMap<>();
        for (String name : List.of(EXCEPTION_FILTER, METRICS_FILTER, CLIENT_FILTER, HANDLER_FILTER)) {
            filters.put(name, new MessageFilter() {
                @Override
                public Response handle(Payload payload) {
                    visited.add(name);
                    return nextMessageFilter == null ? new Response() : nextMessageFilter.handle(payload);
                }
            });
        }
        return new MessageFiltersFacade(filters);
    }

    private List<String> chainOf(boolean metricsEnabled, String... configured) {
        ReflectionTestUtils.setField(config, "metricsEnabled", metricsEnabled);
        InitMessageFilter first = config.messageFilter(new ArrayList<>(List.of(configured)), facade());
        first.handle(new Payload());
        return visited;
    }

    @Test
    @DisplayName("Фильтр метрик встаёт сразу после фильтра исключений")
    void metricsFilterGoesRightAfterExceptionFilter() {
        assertThat(chainOf(true, EXCEPTION_FILTER, CLIENT_FILTER, HANDLER_FILTER))
                .containsExactly(EXCEPTION_FILTER, METRICS_FILTER, CLIENT_FILTER, HANDLER_FILTER);
    }

    @Test
    @DisplayName("Без фильтра исключений фильтр метрик становится первым")
    void withoutExceptionFilterMetricsFilterIsFirst() {
        assertThat(chainOf(true, CLIENT_FILTER, HANDLER_FILTER))
                .containsExactly(METRICS_FILTER, CLIENT_FILTER, HANDLER_FILTER);
    }

    @Test
    @DisplayName("Место, указанное в настройке явно, сохраняется")
    void explicitPositionIsKept() {
        assertThat(chainOf(true, EXCEPTION_FILTER, CLIENT_FILTER, METRICS_FILTER, HANDLER_FILTER))
                .containsExactly(EXCEPTION_FILTER, CLIENT_FILTER, METRICS_FILTER, HANDLER_FILTER);
    }

    @Test
    @DisplayName("При выключенных метриках цепочка остаётся такой, как в настройке")
    void disabledMetricsLeaveChainAsConfigured() {
        assertThat(chainOf(false, EXCEPTION_FILTER, CLIENT_FILTER, HANDLER_FILTER))
                .containsExactly(EXCEPTION_FILTER, CLIENT_FILTER, HANDLER_FILTER);
    }
}
