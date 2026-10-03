package ru.akvine.wild.bot.config;

import com.zaxxer.hikari.HikariDataSource;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import ru.akvine.commons.cluster.lock.ConcurrentOperationsHelper;
import ru.akvine.commons.cluster.lock.SLockProvider;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorage;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorageInDatabaseImpl;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorageInMemoryImpl;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorageInRedisImpl;
import ru.akvine.wild.bot.infrastructure.httplogging.HttpLoggingFilter;
import ru.akvine.wild.bot.infrastructure.httplogging.HttpLoggingFilterFactory;
import ru.akvine.wild.bot.infrastructure.httplogging.HttpLoggingProperties;
import ru.akvine.wild.bot.infrastructure.idempotency.*;
import ru.akvine.wild.bot.infrastructure.lock.DistributedLockProvider;
import ru.akvine.wild.bot.infrastructure.lock.distributed.DataBaseLockProvider;
import ru.akvine.wild.bot.infrastructure.lock.distributed.RedisLockProvider;
import ru.akvine.wild.bot.infrastructure.monitoring.CompositeSqlExecutionListener;
import ru.akvine.wild.bot.infrastructure.monitoring.MonitoringDataSourceProxy;
import ru.akvine.wild.bot.infrastructure.monitoring.SlowQueryLogger;
import ru.akvine.wild.bot.infrastructure.monitoring.SqlExecutionListener;
import ru.akvine.wild.bot.infrastructure.monitoring.api.ApiMetricsCollector;
import ru.akvine.wild.bot.infrastructure.monitoring.api.ApiMetricsFilter;
import ru.akvine.wild.bot.infrastructure.monitoring.api.ApiStatisticsPrinter;
import ru.akvine.wild.bot.infrastructure.monitoring.db.*;
import ru.akvine.wild.bot.infrastructure.monitoring.keystore.*;
import ru.akvine.wild.bot.infrastructure.monitoring.pool.ConnectionPoolMonitor;
import ru.akvine.wild.bot.infrastructure.monitoring.threads.HouseKeeper;
import ru.akvine.wild.bot.infrastructure.monitoring.threads.StackTracePrinter;
import ru.akvine.wild.bot.infrastructure.outbox.DatabaseOutboxStore;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxProperties;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxService;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxStore;
import ru.akvine.wild.bot.infrastructure.resilience.BulkheadFactory;
import ru.akvine.wild.bot.infrastructure.resilience.BulkheadProperties;
import ru.akvine.wild.bot.infrastructure.resilience.CircuitBreakerInterceptorFactory;
import ru.akvine.wild.bot.infrastructure.resilience.CircuitBreakerProperties;
import ru.akvine.wild.bot.infrastructure.retry.DefaultRetryExecutor;
import ru.akvine.wild.bot.infrastructure.retry.ExponentialRetryExecutor;
import ru.akvine.wild.bot.infrastructure.retry.RetryExecutor;
import ru.akvine.wild.bot.infrastructure.role.ConditionalOnJobs;
import ru.akvine.wild.bot.infrastructure.session.*;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.infrastructure.state.StateStorageInDatabaseImpl;
import ru.akvine.wild.bot.infrastructure.state.StateStorageInMemoryImpl;
import ru.akvine.wild.bot.infrastructure.state.StateStorageInRedisImpl;
import ru.akvine.wild.bot.repositories.infrastructure.*;
import ru.akvine.wild.bot.services.AdvertService;
import ru.akvine.wild.bot.services.integration.redis.RedisOperationService;

@Configuration
public class InfrastructureBeansConfig {

    private static final Logger log = LoggerFactory.getLogger(InfrastructureBeansConfig.class);

    @Bean
    @ConditionalOnProperty(name = "states.storage.implementation.type", havingValue = "memory")
    public StateStorage<String, List<ClientState>> memoryStateStorage() {
        return new StateStorageInMemoryImpl();
    }

    @Bean
    @ConditionalOnProperty(name = "states.storage.implementation.type", havingValue = "redis")
    public StateStorage<String, List<ClientState>> redisStateStorage(
            RedisOperationService<ClientState> redisOperationService) {
        return new StateStorageInRedisImpl(redisOperationService);
    }

    @Bean
    @ConditionalOnProperty(name = "states.storage.implementation.type", havingValue = "database")
    public StateStorage<String, List<ClientState>> databaseStateStorage(ClientStatesRepository clientStatesRepository) {
        return new StateStorageInDatabaseImpl(clientStatesRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "session.storage.implementation.type", havingValue = "memory")
    public SessionStorage<String, ClientSessionData> memorySessionStorage() {
        return new SessionStorageInMemoryImpl();
    }

    @Bean
    @ConditionalOnProperty(name = "session.storage.implementation.type", havingValue = "redis")
    public SessionStorage<String, ClientSessionData> redisSessionStorage(
            RedisOperationService<ClientSessionData> redisOperationService) {
        return new SessionStorageInRedisImpl(redisOperationService);
    }

    @Bean
    @ConditionalOnProperty(name = "session.storage.implementation.type", havingValue = "database")
    public SessionStorage<String, ClientSessionData> databaseSessionStorage(
            ClientSessionDataRepository clientSessionDataRepository) {
        return new SessionStorageInDatabaseImpl(clientSessionDataRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "counter.storage.implementation.type", havingValue = "memory")
    public CountersStorage memoryIterationsStorage(AdvertService advertService) {
        return new CountersStorageInMemoryImpl(advertService);
    }

    @Bean
    @ConditionalOnProperty(name = "counter.storage.implementation.type", havingValue = "redis")
    public CountersStorage redisIterationsStorage(RedisOperationService<Long> redisOperationService) {
        return new CountersStorageInRedisImpl(redisOperationService);
    }

    @Bean
    @ConditionalOnProperty(name = "counter.storage.implementation.type", havingValue = "database")
    public CountersStorage databaseIterationsStorage(IterationCounterRepository iterationCounterRepository) {
        return new CountersStorageInDatabaseImpl(iterationCounterRepository);
    }

    @Bean
    @ConditionalOnProperty(
            name = "retry.executor.implementation.type",
            havingValue = "exponential",
            matchIfMissing = true)
    public RetryExecutor exponentialRetryExecutor(
            @Value("${send.file.retry.attempts.count}") int attempts,
            @Value("${send.file.retry.initial.delay.millis}") int retryInitialDelayMillis,
            @Value("${send.file.retry.exponential.backoff.multiplier}") double retryExponentialBackoffMultiplier,
            @Value("${send.file.retry.max.delay.millis}") int retryMaxDelayMillis,
            @Value("${send.file.retry.jitter.factor:0.2}") double retryJitterFactor) {

        return new ExponentialRetryExecutor(
                attempts,
                Duration.ofMillis(retryInitialDelayMillis),
                retryExponentialBackoffMultiplier,
                Duration.ofMillis(retryMaxDelayMillis),
                retryJitterFactor);
    }

    @Bean
    @ConditionalOnProperty(name = "retry.executor.implementation.type", havingValue = "default")
    public RetryExecutor defaultRetryExecutor(
            @Value("${send.file.retry.attempts.count}") int attempts,
            @Value("${send.file.retry.initial.delay.millis}") int retryInitialDelayMillis) {
        return new DefaultRetryExecutor(attempts, Duration.ofMillis(retryInitialDelayMillis));
    }

    @Bean
    @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true")
    public DistributedLockProvider redisLockProvider(RedissonClient redisson) {
        return new RedisLockProvider(redisson);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "false")
    public DistributedLockProvider databaseLockProvider(
            ConcurrentOperationsHelper concurrentOperationsHelper,
            SLockProvider sLockProvider,
            TransactionTemplate transactionTemplate) {
        return new DataBaseLockProvider(concurrentOperationsHelper, sLockProvider, transactionTemplate);
    }

    /**
     * Периодически пишет в лог состояние пула соединений HikariCP (всего/занято/свободно). Бину
     * нужен именно {@link HikariDataSource}: при включённом мониторинге медленных запросов это
     * оригинальный пул под {@link Primary}-обёрткой.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(name = "monitoring.connection.pool.enabled", havingValue = "true")
    public ConnectionPoolMonitor connectionPoolMonitor(
            HikariDataSource hikariDataSource,
            @Value("${monitoring.connection.pool.print.interval.milliseconds}") long intervalMillis) {
        return new ConnectionPoolMonitor(hikariDataSource, intervalMillis, hikariDataSource.getPoolName());
    }

    /**
     * Периодический дамп стеков всех потоков в {@code diagnostic.stack.trace.dir}. Запускается и
     * останавливается вместе с контекстом (init/destroy-методы).
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(name = "diagnostic.stack.trace.enabled", havingValue = "true")
    public StackTracePrinter stackTracePrinter(
            @Value("${diagnostic.stack.trace.dir}") String dir,
            @Value("${diagnostic.stack.trace.interval.milliseconds}") long intervalMillis) {
        return new StackTracePrinter(dir, intervalMillis);
    }

    /**
     * Раз в час архивирует дампы старше 2 часов в {@code <dir>-archive} и удаляет архивы старше 7
     * дней (правила - {@link StackTracePrinter#simpleHouseKeeperConfig}).
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(name = "diagnostic.stack.trace.enabled", havingValue = "true")
    public HouseKeeper stackTraceHouseKeeper(@Value("${diagnostic.stack.trace.dir}") String dir) {
        return new HouseKeeper(
                StackTracePrinter.simpleHouseKeeperConfig(dir),
                Duration.ofHours(1).toMillis());
    }

    /**
     * Как только в контексте появляется свой бин {@link DataSource}, автоконфигурация Spring Boot
     * перестаёт создавать пул сама, поэтому при включённом мониторинге запросов (медленные запросы
     * или {@code db.metrics}) {@link HikariDataSource} объявляется явно - с теми же
     * {@code spring.datasource.*} и {@code spring.datasource.hikari.*} настройками. Именно на него
     * по типу смотрит {@code ScheduledConfig#hikariPoolMetricsJob}. Когда оба мониторинга выключены
     * (или свойств нет), бин не создаётся и пул поднимает автоконфигурация, как раньше.
     */
    @Bean
    @ConditionalOnExpression("${monitoring.slow.query.enabled:false} or ${db.metrics.enabled:false}")
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource hikariDataSource(DataSourceProperties properties) {
        return properties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * {@link Primary}-обёртка над {@link HikariDataSource}, сообщающая всем включённым слушателям
     * ({@link SqlExecutionListener}: {@link SlowQueryLogger}, {@link DbMetricsListener}) о каждом
     * запросе и коммите (см. {@link MonitoringDataSourceProxy}): все обычные потребители
     * {@code DataSource} (JPA, Liquibase, локи и т.д.) получают её, а код, которому нужен именно
     * {@code HikariDataSource}, - оригинал.
     */
    @Bean
    @Primary
    @ConditionalOnExpression("${monitoring.slow.query.enabled:false} or ${db.metrics.enabled:false}")
    public DataSource monitoredDataSource(HikariDataSource hikariDataSource, List<SqlExecutionListener> listeners) {
        log.info("Database monitoring enabled, listeners: {}", listeners);
        return new MonitoringDataSourceProxy(hikariDataSource, new CompositeSqlExecutionListener(listeners));
    }

    /**
     * Пишет в отдельный лог запросы дольше порога {@code monitoring.slow.query.threshold.milliseconds}.
     */
    @Bean
    @ConditionalOnProperty(name = "monitoring.slow.query.enabled", havingValue = "true")
    public SlowQueryLogger slowQueryLogger(
            @Value("${monitoring.slow.query.threshold.milliseconds:3000}") long thresholdMillis) {
        return new SlowQueryLogger(thresholdMillis);
    }

    /**
     * Реестр JMX-метрик БД (общий счётчик коммитов); домен - {@code db.metrics.jmx.domain}.
     */
    @Bean(destroyMethod = "stop")
    @ConditionalOnProperty(name = "db.metrics.enabled", havingValue = "true")
    public JmxMetrics dbJmxMetrics(@Value("${db.metrics.jmx.domain:wild.bot.db}") String domain) {
        JmxMetrics jmxMetrics = new JmxMetrics(domain);
        jmxMetrics.start();
        return jmxMetrics;
    }

    /**
     * Метрики работы с БД по группам потоков: число коммитов и запросов, подходящих под заданные
     * регулярные выражения, логирование коммитов со стеком вызовов, общий счётчик коммитов в JMX.
     * Настраивается свойствами {@code db.metrics.*} (см. {@link DbMetricsConfigurer}), меняется на
     * лету через {@link DbMetricsService}.
     */
    @Bean
    @ConditionalOnProperty(name = "db.metrics.enabled", havingValue = "true")
    public DbMetricsService dbMetricsService(Environment environment, JmxMetrics dbJmxMetrics) {
        Map<String, String> properties = Binder.get(environment)
                .bind("db.metrics", Bindable.mapOf(String.class, String.class))
                .orElseGet(Map::of);
        return DbMetricsConfigurer.configure(properties, dbJmxMetrics);
    }

    @Bean
    @ConditionalOnProperty(name = "db.metrics.enabled", havingValue = "true")
    public DbMetricsListener dbMetricsListener(DbMetricsService dbMetricsService) {
        return new DbMetricsListener(dbMetricsService.getMetrics());
    }

    /**
     * Публикует в JMX, к какой базе подключено приложение (хост, порт, имя базы, пользователь).
     */
    @Bean
    @ConditionalOnProperty(name = "db.metrics.enabled", havingValue = "true")
    public DbConnectionJmxRegister dbConnectionJmxRegister(
            DataSourceProperties dataSourceProperties, @Value("${db.metrics.jmx.domain:wild.bot.db}") String domain)
            throws Exception {
        DbInfoConnection connection =
                new JdbcUrlParser().parseUrl(dataSourceProperties.getUrl(), dataSourceProperties.getUsername());
        return connection == null ? null : new DbConnectionJmxRegister(domain + ".monitoring", connection);
    }

    /**
     * Настройки логирования HTTP-запросов и ответов - свойства {@code http.logging.*}
     */
    @Bean
    @ConditionalOnProperty(name = "http.logging.enabled", havingValue = "true")
    @ConfigurationProperties("http.logging")
    public HttpLoggingProperties httpLoggingProperties() {
        return new HttpLoggingProperties();
    }

    /**
     * Регистрирует {@link HttpLoggingFilter} в цепочке сервлет-фильтров сразу после
     * {@code CharacterEncodingFilter} и до Spring Security, поэтому в лог попадают и запросы,
     * отклонённые авторизацией.
     */
    @Bean
    @ConditionalOnProperty(name = "http.logging.enabled", havingValue = "true")
    public FilterRegistrationBean<HttpLoggingFilter> httpLoggingFilter(
            HttpLoggingProperties httpLoggingProperties, ObjectProvider<RequestMappingHandlerMapping> handlerMappings) {
        log.info("Http logging enabled");
        FilterRegistrationBean<HttpLoggingFilter> registration =
                new FilterRegistrationBean<>(HttpLoggingFilterFactory.create(httpLoggingProperties, handlerMappings));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }

    /**
     * Codahale-таймеры по эндпоинтам, доступные через JMX (см. {@link ApiMetricsCollector}).
     */
    @Bean(destroyMethod = "stop")
    @ConditionalOnProperty(name = "monitoring.api.metrics.enabled", havingValue = "true")
    public ApiMetricsCollector apiMetricsCollector(
            ApplicationContext applicationContext, @Value("${monitoring.api.metrics.domain}") String domain) {
        return new ApiMetricsCollector(applicationContext, domain);
    }

    /**
     * Замеряет время обработки каждого запроса; стоит вне цепочки Security, чтобы в замер входила
     * вся обработка.
     */
    @Bean
    @ConditionalOnProperty(name = "monitoring.api.metrics.enabled", havingValue = "true")
    public FilterRegistrationBean<ApiMetricsFilter> apiMetricsFilter(ApiMetricsCollector apiMetricsCollector) {
        FilterRegistrationBean<ApiMetricsFilter> registration =
                new FilterRegistrationBean<>(new ApiMetricsFilter(apiMetricsCollector));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        return registration;
    }

    /**
     * Периодически пишет таблицу статистики по эндпоинтам в отдельный лог.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(
            name = {"monitoring.api.metrics.enabled", "monitoring.api.metrics.print.enabled"},
            havingValue = "true")
    public ApiStatisticsPrinter apiStatisticsPrinter(
            ApiMetricsCollector apiMetricsCollector,
            @Value("${monitoring.api.metrics.print.interval.minutes}") long intervalMinutes) {
        return new ApiStatisticsPrinter(apiMetricsCollector, intervalMinutes);
    }

    /**
     * Настройки слежения за сертификатами в ключницах - свойства {@code monitoring.keystore.*}
     */
    @Bean
    @ConditionalOnProperty(name = "monitoring.keystore.enabled", havingValue = "true")
    @ConfigurationProperties("monitoring.keystore")
    public KeystoreMonitoringProperties keystoreMonitoringProperties() {
        return new KeystoreMonitoringProperties();
    }

    @Bean
    @ConditionalOnProperty(name = "monitoring.keystore.enabled", havingValue = "true")
    public KeyStoreObserver fileKeyStoreObserver(
            KeystoreMonitoringProperties properties, ResourceLoader resourceLoader) {
        List<KeyStoreConfig> keyStoreConfigs = properties.getKeystores().stream()
                .map(keystore -> new KeyStoreConfig(
                        keystore.getType(),
                        resourceLoader.getResource(keystore.getLocation()),
                        keystore.getProvider(),
                        keystore.getPassword()))
                .toList();
        return new FileKeyStoreObserver(
                keyStoreConfigs, properties.getDaysToExpire(), true, properties.getDaysExpired());
    }

    /**
     * Раз в {@code monitoring.keystore.check-interval-hours} часов пишет в лог сертификаты,
     * которые скоро истекут или уже истекли.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(name = "monitoring.keystore.enabled", havingValue = "true")
    public KeystoreExpirationMonitor keystoreExpirationMonitor(
            KeyStoreObserver keyStoreObserver, KeystoreMonitoringProperties properties) {
        return new KeystoreExpirationMonitor(
                keyStoreObserver, properties.getCheckIntervalHours(), properties.getDaysToExpire());
    }

    /**
     * Настройки circuit breaker'ов внешних систем - свойства {@code circuit.breaker.*}
     */
    @Bean
    @ConfigurationProperties("circuit.breaker")
    public CircuitBreakerProperties circuitBreakerProperties() {
        return new CircuitBreakerProperties();
    }

    /**
     * Выдаёт {@code RestTemplate}-перехватчики с circuit breaker'ом по имени внешней системы. Бин
     * есть всегда: при {@code circuit.breaker.enabled=false} перехватчики просто пропускают вызовы.
     */
    @Bean
    public CircuitBreakerInterceptorFactory circuitBreakerInterceptorFactory(CircuitBreakerProperties properties) {
        return new CircuitBreakerInterceptorFactory(properties);
    }

    /**
     * Настройки bulkhead'ов внешних систем - свойства {@code bulkhead.*}
     */
    @Bean
    @ConfigurationProperties("bulkhead")
    public BulkheadProperties bulkheadProperties() {
        return new BulkheadProperties();
    }

    /**
     * Ограничивает число одновременных вызовов к внешней системе (в целом и на одного клиента), чтобы зависшая
     * система не заняла все потоки приложения. Бин есть всегда: при {@code bulkhead.enabled=false} ничего не
     * ограничивает.
     */
    @Bean
    public BulkheadFactory bulkheadFactory(BulkheadProperties properties) {
        return new BulkheadFactory(properties);
    }

    /**
     * Настройки идемпотентности - свойства {@code idempotency.*}
     */
    @Bean
    @ConfigurationProperties("idempotency")
    public IdempotencyProperties idempotencyProperties() {
        return new IdempotencyProperties();
    }

    @Bean
    public IdempotencyPayloadSerializer idempotencyPayloadSerializer() {
        return new IdempotencyPayloadSerializer();
    }

    /**
     * Ключи идемпотентности в памяти (по умолчанию): защищает от дублей только в пределах одного инстанса
     * и не переживает рестарт.
     */
    @Bean
    @ConditionalOnProperty(
            name = "idempotency.store.implementation.type",
            havingValue = "memory",
            matchIfMissing = true)
    public IdempotencyStore memoryIdempotencyStore() {
        return new InMemoryIdempotencyStore();
    }

    /**
     * Ключи идемпотентности в БД: общие для всех инстансов, переживают рестарт. Требуют таблицу
     * {@code IDEMPOTENCY_KEY_ENTITY} (миграция Liquibase).
     */
    @Bean
    @ConditionalOnProperty(name = "idempotency.store.implementation.type", havingValue = "database")
    public DatabaseIdempotencyStore databaseIdempotencyStore(IdempotencyKeyRepository idempotencyKeyRepository) {
        return new DatabaseIdempotencyStore(idempotencyKeyRepository);
    }

    /**
     * Ключи идемпотентности в Redis: общие для всех инстансов, истекают по сроку жизни ключа Redis.
     * Требуют {@code spring.redis.enabled=true}.
     */
    @Bean
    @ConditionalOnProperty(name = "idempotency.store.implementation.type", havingValue = "redis")
    public IdempotencyStore redisIdempotencyStore(
            RedisOperationService<String> redisOperationService, IdempotencyPayloadSerializer serializer) {
        return new RedisIdempotencyStore(redisOperationService, serializer);
    }

    /**
     * Очистка истёкших ключей идемпотентности в БД.
     */
    @Bean
    @ConditionalOnProperty(name = "idempotency.store.implementation.type", havingValue = "database")
    @ConditionalOnJobs
    public IdempotencyCleanupJob idempotencyCleanupJob(DatabaseIdempotencyStore databaseIdempotencyStore) {
        return new IdempotencyCleanupJob(databaseIdempotencyStore);
    }

    @Bean
    public IdempotencyService idempotencyService(
            IdempotencyStore idempotencyStore, IdempotencyPayloadSerializer serializer) {
        return new IdempotencyService(idempotencyStore, serializer);
    }

    /**
     * Идемпотентность HTTP API по заголовку {@code Idempotency-Key}. Фильтр включается в цепочку Spring
     * Security (см. {@code SecurityConfig}), поэтому в контейнер сервлетов отдельно не регистрируется.
     */
    @Bean
    @ConditionalOnProperty(name = "idempotency.http.enabled", havingValue = "true")
    public IdempotencyFilter idempotencyFilter(
            IdempotencyService idempotencyService,
            IdempotencyPayloadSerializer serializer,
            IdempotencyProperties idempotencyProperties) {
        return new IdempotencyFilter(idempotencyService, serializer, idempotencyProperties.getHttp());
    }

    @Bean
    @ConditionalOnProperty(name = "idempotency.http.enabled", havingValue = "true")
    public FilterRegistrationBean<IdempotencyFilter> idempotencyFilterRegistration(IdempotencyFilter filter) {
        FilterRegistrationBean<IdempotencyFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * Настройки transactional outbox - свойства {@code outbox.*}
     */
    @Bean
    @ConfigurationProperties("outbox")
    public OutboxProperties outboxProperties() {
        return new OutboxProperties();
    }

    /**
     * Outbox в БД: сообщение пишется в той же транзакции, что и бизнес-изменение. Требует таблицу
     * {@code OUTBOX_MESSAGE_ENTITY} (миграция Liquibase).
     */
    @Bean
    public OutboxStore outboxStore(OutboxMessageRepository outboxMessageRepository) {
        return new DatabaseOutboxStore(outboxMessageRepository);
    }

    @Bean
    public OutboxService outboxService(OutboxStore outboxStore) {
        return new OutboxService(outboxStore);
    }
}
