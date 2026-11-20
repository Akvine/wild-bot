package ru.akvine.wild.bot.unit.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import ru.akvine.wild.bot.config.ProxyConfig;
import ru.akvine.wild.bot.enums.ProxyType;
import ru.akvine.wild.bot.facades.proxy.WildberriesProxiesFacade;
import ru.akvine.wild.bot.infrastructure.monitoring.micrometer.IntegrationMetrics;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.wild.bot.services.integration.wildberries.proxy.WildberriesIntegrationServiceProxy;

@DisplayName("Метрики обращений к Wildberries")
class ProxyConfigMetricsTest {
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final WildberriesIntegrationService origin = mock(WildberriesIntegrationService.class);
    private final WildberriesProxiesFacade proxies = new WildberriesProxiesFacade(Map.of());

    @SuppressWarnings("unchecked")
    private ObjectProvider<IntegrationMetrics> providerOf(IntegrationMetrics metrics) {
        ObjectProvider<IntegrationMetrics> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(metrics);
        return provider;
    }

    private WildberriesIntegrationService service(IntegrationMetrics metrics) {
        return new ProxyConfig().wildberriesIntegrationService(proxies, origin, new ArrayList<>(), providerOf(metrics));
    }

    @Test
    @DisplayName("При включённых метриках вызовы сервиса доходят до Wildberries и считаются")
    void callsAreMeteredWhenMetricsAreOn() {
        when(origin.getCards("token")).thenReturn(List.of());

        WildberriesIntegrationService service = service(new IntegrationMetrics(registry));
        service.getCards("token");
        service.pauseAdvert(7, "token");

        verify(origin).getCards("token");
        verify(origin).pauseAdvert(7, "token");
        assertThat(registry.find(IntegrationMetrics.REQUESTS)
                        .tags("system", "wildberries", "operation", "getCards", "outcome", "success")
                        .timer()
                        .count())
                .isEqualTo(1);
        assertThat(registry.find(IntegrationMetrics.REQUESTS)
                        .tags("system", "wildberries", "operation", "pauseAdvert")
                        .timer()
                        .count())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Замер стоит снаружи цепочки прокси: вызов уходит во внешний прокси и считается один раз")
    void wholeProxyChainIsMetered() {
        WildberriesIntegrationServiceProxy proxy = mock(WildberriesIntegrationServiceProxy.class);
        WildberriesProxiesFacade withProxy = new WildberriesProxiesFacade(Map.of(ProxyType.VALIDATION, proxy));

        WildberriesIntegrationService service = new ProxyConfig()
                .wildberriesIntegrationService(
                        withProxy,
                        origin,
                        new ArrayList<>(List.of("validation")),
                        providerOf(new IntegrationMetrics(registry)));
        service.getCards("token");

        verify(proxy).setTargetObject(origin);
        verify(proxy).getCards("token");
        assertThat(registry.find(IntegrationMetrics.REQUESTS).timers()).hasSize(1);
        assertThat(registry.find(IntegrationMetrics.REQUESTS)
                        .tags("system", "wildberries", "operation", "getCards")
                        .timer()
                        .count())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("При выключенных метриках сервис отдаётся как есть, без обёртки")
    void withoutMetricsServiceIsReturnedAsIs() {
        assertThat(service(null)).isSameAs(origin);
    }
}
