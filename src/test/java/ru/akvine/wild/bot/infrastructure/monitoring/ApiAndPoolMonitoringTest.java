package ru.akvine.wild.bot.infrastructure.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.codahale.metrics.Timer;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import ru.akvine.wild.bot.infrastructure.monitoring.api.ApiMetricsCollector;
import ru.akvine.wild.bot.infrastructure.monitoring.api.ApiMetricsFilter;
import ru.akvine.wild.bot.infrastructure.monitoring.api.ApiStatisticsPrinter;
import ru.akvine.wild.bot.infrastructure.monitoring.api.EndpointMetric;
import ru.akvine.wild.bot.infrastructure.monitoring.pool.ConnectionPoolMonitor;
import ru.akvine.wild.bot.infrastructure.monitoring.pool.MonitorableConnectionPool;
import ru.akvine.wild.bot.infrastructure.monitoring.pool.MonitorableConnectionPoolFactory;

@DisplayName("Мониторинг API и пула соединений")
class ApiAndPoolMonitoringTest {

    private ApiMetricsCollector collector(String... patterns) {
        ApplicationContext context = mock(ApplicationContext.class);
        RequestMappingHandlerMapping mapping = mock(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> methods = new java.util.LinkedHashMap<>();
        for (String pattern : patterns) {
            methods.put(RequestMappingInfo.paths(pattern).build(), mock(HandlerMethod.class));
        }
        when(mapping.getHandlerMethods()).thenReturn(methods);
        when(context.getBeansOfType(RequestMappingHandlerMapping.class)).thenReturn(Map.of("mapping", mapping));
        ApiMetricsCollector collector = new ApiMetricsCollector(context, "test.api.metrics" + System.nanoTime());
        collector.onApplicationEvent(new ContextRefreshedEvent(context));
        return collector;
    }

    @Test
    @DisplayName("Коллектор находит эндпоинты, создаёт таймер при первом обращении и игнорирует чужие события")
    void collectorRegistersEndpoints() {
        ApiMetricsCollector collector = collector("/admin/list", "/admin/{id}");
        try {
            assertThat(collector.getEndpointMetrics())
                    .extracting(EndpointMetric::getName)
                    .containsExactlyInAnyOrder("REST.ADMIN_LIST.counters", "REST.ADMIN_ID.counters");

            Timer timer = collector.getOrCreateTimer("/admin/list");
            assertThat(timer).isNotNull();
            assertThat(collector.getOrCreateTimer("/admin/list")).isSameAs(timer);
            assertThat(collector.getOrCreateTimer("/unknown")).isNull();

            ApplicationContext other = mock(ApplicationContext.class);
            collector.onApplicationEvent(new ContextRefreshedEvent(other));
            assertThat(collector.getEndpointMetrics()).hasSize(2);
        } finally {
            collector.stop();
        }
    }

    @Test
    @DisplayName("Фильтр записывает время запроса в таймер эндпоинта по шаблону пути")
    void filterRecordsRequestTime() throws Exception {
        ApiMetricsCollector collector = collector("/admin/{id}");
        try {
            ApiMetricsFilter filter = new ApiMetricsFilter(collector);
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/5");
            request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/admin/{id}");
            filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {});
            filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {});

            assertThat(collector.getOrCreateTimer("/admin/{id}").getCount()).isEqualTo(2);

            MockHttpServletRequest noPattern = new MockHttpServletRequest("GET", "/x");
            filter.doFilter(noPattern, new MockHttpServletResponse(), (req, res) -> {});
            MockHttpServletRequest unknown = new MockHttpServletRequest("GET", "/y");
            unknown.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/unknown");
            filter.doFilter(unknown, new MockHttpServletResponse(), (req, res) -> {});
            assertThat(collector.getOrCreateTimer("/admin/{id}").getCount()).isEqualTo(2);
        } finally {
            collector.stop();
        }
    }

    @Test
    @DisplayName("Фильтр записывает время и тогда, когда обработка запроса упала")
    void filterRecordsEvenWhenChainFails() {
        ApiMetricsCollector collector = collector("/fail");
        try {
            ApiMetricsFilter filter = new ApiMetricsFilter(collector);
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/fail");
            request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/fail");

            assertThatThrownBy(() -> filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
                        throw new java.io.IOException("boom");
                    }))
                    .isInstanceOf(java.io.IOException.class);

            assertThat(collector.getOrCreateTimer("/fail").getCount()).isEqualTo(1);
        } finally {
            collector.stop();
        }
    }

    @Test
    @DisplayName("Принтер выводит таблицу: вызванные эндпоинты по убыванию счётчика, затем невызванные")
    void printerRendersTable() {
        ApiMetricsCollector collector = collector("/a", "/b", "/c");
        Logger logger = (Logger) LoggerFactory.getLogger("ru.akvine.wild.bot.monitoring.ApiStatistics");
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            collector.getOrCreateTimer("/a").update(5, java.util.concurrent.TimeUnit.MILLISECONDS);
            Timer busy = collector.getOrCreateTimer("/b");
            busy.update(1, java.util.concurrent.TimeUnit.MILLISECONDS);
            busy.update(1, java.util.concurrent.TimeUnit.MILLISECONDS);

            ApiStatisticsPrinter printer = new ApiStatisticsPrinter(collector, 1);
            printer.printEndpointStatistic();

            String table = appender.list.get(0).getFormattedMessage();
            assertThat(table).contains("Endpoint").contains("REST.A.counters").contains("REST.C.counters");
            assertThat(table.indexOf("REST.B.counters")).isLessThan(table.indexOf("REST.A.counters"));
            assertThat(table.indexOf("REST.A.counters")).isLessThan(table.indexOf("REST.C.counters"));

            printer.start();
            printer.stop();
        } finally {
            logger.detachAppender(appender);
            collector.stop();
        }
    }

    @Test
    @DisplayName("Принтер: интервал должен быть не меньше минуты, ошибка рендера не пробрасывается")
    void printerValidation() {
        assertThatThrownBy(() -> new ApiStatisticsPrinter(null, 0)).isInstanceOf(IllegalArgumentException.class);

        new ApiStatisticsPrinter(null, 1).printEndpointStatistic();
    }

    private static HikariDataSource lazyPool(String name) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:" + name + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        dataSource.setMaximumPoolSize(4);
        dataSource.setPoolName(name);
        return dataSource;
    }

    @Test
    @DisplayName("Пул Hikari: до запуска состояние неизвестно, после первого соединения - заполнено")
    void hikariPoolState() throws Exception {
        try (HikariDataSource dataSource = lazyPool("state-pool")) {
            MonitorableConnectionPool pool = MonitorableConnectionPoolFactory.create(dataSource, "state-pool");
            assertThat(pool.getPoolName()).isEqualTo("state-pool");
            int unknown = MonitorableConnectionPool.POOL_NOT_YET_INITIALIZED_VALUE;
            assertThat(pool.getTotalConnections()).isEqualTo(unknown);
            assertThat(pool.getBusyConnections()).isEqualTo(unknown);
            assertThat(pool.getIdleConnections()).isEqualTo(unknown);
            assertThat(pool.getMaxPoolSize()).isEqualTo(unknown);

            pool.enableConnectionAcquiringMonitor();
            try (Connection connection = dataSource.getConnection()) {
                assertThat(connection.isValid(1)).isTrue();
                assertThat(pool.getBusyConnections()).isEqualTo(1);
                assertThat(pool.getTotalConnections()).isPositive();
                assertThat(pool.getIdleConnections()).isGreaterThanOrEqualTo(0);
                assertThat(pool.getMaxPoolSize()).isEqualTo(4);
            }
        }
    }

    @Test
    @DisplayName("Монитор пула: пишет состояние пула, а до инициализации - предупреждение")
    void connectionPoolMonitorLogsState() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(ConnectionPoolMonitor.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try (HikariDataSource dataSource = lazyPool("logged-pool")) {
            ConnectionPoolMonitor monitor = new ConnectionPoolMonitor(dataSource, 1000, "logged-pool");
            monitor.start();

            ReflectionTestUtils.invokeMethod(monitor, "logDatabaseConnectionPoolState");
            assertThat(appender.list.get(0).getFormattedMessage()).contains("pool not initialized yet");

            try (Connection connection = dataSource.getConnection()) {
                assertThat(connection.isValid(1)).isTrue();
                ReflectionTestUtils.invokeMethod(monitor, "logDatabaseConnectionPoolState");
            }
            assertThat(appender.list.get(1).getFormattedMessage())
                    .contains("CONNECTION_POOL_STATE [logged-pool]")
                    .contains("busy: 1");
            monitor.stop();
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    @DisplayName("Фабрика пулов не знает чужих DataSource; монитор проверяет параметры")
    void poolFactoryRejectsUnknownDataSource() {
        DataSource unknown = mock(DataSource.class);

        assertThatThrownBy(() -> MonitorableConnectionPoolFactory.create(unknown, "p"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HikariDataSource");
        assertThatThrownBy(() -> new ConnectionPoolMonitor(null, 1000, "p")).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ConnectionPoolMonitor(unknown, 10, "p"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
