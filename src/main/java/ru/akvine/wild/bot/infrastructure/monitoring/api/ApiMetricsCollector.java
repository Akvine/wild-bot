package ru.akvine.wild.bot.infrastructure.monitoring.api;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.Timer;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Хранит по {@link Timer} на каждый эндпоинт из {@code @RequestMapping} и публикует их в JMX
 * (домен - {@code domain}, имя метрики - {@code REST.<ПУТЬ>.counters}). Список эндпоинтов собирается
 * при старте контекста, таймер эндпоинта создаётся при первом запросе к нему; статистика
 * в таймере - за последнюю минуту (скользящее окно).
 */
@Slf4j
public class ApiMetricsCollector implements ApplicationListener<ContextRefreshedEvent> {
    private static final int WINDOW_MINUTES = 1;

    private final ApplicationContext applicationContext;
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final JmxReporter jmxReporter;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    /** Все эндпоинты приложения по шаблону пути (например {@code /admin/adverts/list}) */
    private final Map<String, EndpointMetric> endpointMetrics = new ConcurrentHashMap<>();

    public ApiMetricsCollector(ApplicationContext applicationContext, String domain) {
        this.applicationContext = applicationContext;
        this.jmxReporter = JmxReporter.forRegistry(metricRegistry).inDomain(domain).build();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext() != applicationContext || !initialized.compareAndSet(false, true)) {
            return;
        }
        initEndpointMetrics();
        jmxReporter.start();
    }

    public void stop() {
        jmxReporter.stop();
    }

    /**
     * @param pattern шаблон пути эндпоинта, как в маппинге контроллера
     * @return таймер эндпоинта или {@code null}, если такого эндпоинта нет
     */
    public Timer getOrCreateTimer(String pattern) {
        EndpointMetric endpointMetric = endpointMetrics.get(pattern);
        if (endpointMetric == null) {
            return null;
        }
        Timer timer = endpointMetric.getTimer();
        if (timer != null) {
            return timer;
        }
        synchronized (endpointMetric) {
            timer = endpointMetric.getTimer();
            if (timer == null) {
                timer = metricRegistry.register(
                        endpointMetric.getName(),
                        new Timer(new SlidingTimeWindowReservoir(WINDOW_MINUTES, TimeUnit.MINUTES)));
                endpointMetric.setTimer(timer);
            }
            return timer;
        }
    }

    public Collection<EndpointMetric> getEndpointMetrics() {
        return endpointMetrics.values();
    }

    private void initEndpointMetrics() {
        applicationContext.getBeansOfType(RequestMappingHandlerMapping.class).values().forEach(mapping ->
                mapping.getHandlerMethods().keySet().forEach(info -> info.getPatternValues()
                        .forEach(pattern -> endpointMetrics.put(pattern, new EndpointMetric(metricName(pattern))))));
        logger.info("Endpoint statistics. [{}] endpoints found", endpointMetrics.size());
    }

    /**
     * {@code /admin/adverts/list} -> {@code REST.ADMIN_ADVERTS_LIST.counters}
     */
    static String metricName(String pattern) {
        String endpoint = pattern.replaceFirst("/", "")
                .replaceAll("_", "-")
                .replaceAll("[./]", "_")
                .replaceAll("[*{}]", "")
                .toUpperCase();
        return String.join(".", "REST", endpoint, "counters");
    }
}
