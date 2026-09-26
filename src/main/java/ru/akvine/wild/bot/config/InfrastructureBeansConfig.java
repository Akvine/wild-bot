package ru.akvine.wild.bot.config;

import com.zaxxer.hikari.HikariDataSource;
import java.time.Duration;
import java.util.List;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import javax.sql.DataSource;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.client.RestTemplate;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import ru.akvine.commons.cluster.lock.ConcurrentOperationsHelper;
import ru.akvine.commons.cluster.lock.SLockProvider;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorage;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorageInDatabaseImpl;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorageInMemoryImpl;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorageInRedisImpl;
import ru.akvine.wild.bot.infrastructure.http.CommonHttpClientBuilder;
import ru.akvine.wild.bot.infrastructure.http.HttpClientBuilderFactory;
import ru.akvine.wild.bot.infrastructure.http.HttpClientProperties;
import ru.akvine.wild.bot.infrastructure.http.keystore.DefaultKeystoreFactory;
import ru.akvine.wild.bot.infrastructure.http.keystore.SslContextUtils;
import ru.akvine.wild.bot.infrastructure.httplogging.HttpLoggingFilter;
import ru.akvine.wild.bot.infrastructure.httplogging.HttpLoggingFilterFactory;
import ru.akvine.wild.bot.infrastructure.httplogging.HttpLoggingProperties;
import ru.akvine.wild.bot.infrastructure.lock.DistributedLockProvider;
import ru.akvine.wild.bot.infrastructure.lock.distributed.DataBaseLockProvider;
import ru.akvine.wild.bot.infrastructure.lock.distributed.RedisLockProvider;
import ru.akvine.wild.bot.infrastructure.monitoring.SlowQueryDataSourceProxy;
import ru.akvine.wild.bot.infrastructure.monitoring.SlowQueryLogger;
import ru.akvine.wild.bot.infrastructure.monitoring.api.ApiMetricsCollector;
import ru.akvine.wild.bot.infrastructure.monitoring.api.ApiMetricsFilter;
import ru.akvine.wild.bot.infrastructure.monitoring.api.ApiStatisticsPrinter;
import ru.akvine.wild.bot.infrastructure.monitoring.keystore.FileKeyStoreObserver;
import ru.akvine.wild.bot.infrastructure.monitoring.keystore.KeyStoreConfig;
import ru.akvine.wild.bot.infrastructure.monitoring.keystore.KeyStoreObserver;
import ru.akvine.wild.bot.infrastructure.monitoring.keystore.KeystoreExpirationMonitor;
import ru.akvine.wild.bot.infrastructure.monitoring.keystore.KeystoreMonitoringProperties;
import ru.akvine.wild.bot.infrastructure.monitoring.pool.ConnectionPoolMonitor;
import ru.akvine.wild.bot.infrastructure.monitoring.threads.HouseKeeper;
import ru.akvine.wild.bot.infrastructure.monitoring.threads.StackTracePrinter;
import ru.akvine.wild.bot.infrastructure.retry.DefaultRetryExecutor;
import ru.akvine.wild.bot.infrastructure.retry.ExponentialRetryExecutor;
import ru.akvine.wild.bot.infrastructure.retry.RetryExecutor;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.infrastructure.session.SessionStorageInDatabaseImpl;
import ru.akvine.wild.bot.infrastructure.session.SessionStorageInMemoryImpl;
import ru.akvine.wild.bot.infrastructure.session.SessionStorageInRedisImpl;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.infrastructure.state.StateStorageInDatabaseImpl;
import ru.akvine.wild.bot.infrastructure.state.StateStorageInMemoryImpl;
import ru.akvine.wild.bot.infrastructure.state.StateStorageInRedisImpl;
import ru.akvine.wild.bot.services.integration.redis.RedisOperationService;
import ru.akvine.wild.bot.repositories.infrastructure.ClientSessionDataRepository;
import ru.akvine.wild.bot.repositories.infrastructure.ClientStatesRepository;
import ru.akvine.wild.bot.repositories.infrastructure.IterationCounterRepository;
import ru.akvine.wild.bot.services.AdvertService;

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
        return new HouseKeeper(StackTracePrinter.simpleHouseKeeperConfig(dir), Duration.ofHours(1).toMillis());
    }

    /**
     * Как только в контексте появляется свой бин {@link DataSource},автоконфигурация Spring Boot
     * перестаёт создавать пул сама, поэтому при включённом мониторинге медленных запросов
     * {@link HikariDataSource} объявляется явно - с теми же {@code spring.datasource.*} и
     * {@code spring.datasource.hikari.*} настройками. Именно на него по типу смотрит
     * {@code ScheduledConfig#hikariPoolMetricsJob}. При {@code monitoring.slow.query.enabled=false}
     * (или отсутствии свойства) бин не создаётся и пул поднимает автоконфигурация, как раньше.
     */
    @Bean
    @ConditionalOnProperty(name = "monitoring.slow.query.enabled", havingValue = "true")
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource hikariDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    /**
     * {@link Primary}-обёртка над {@link HikariDataSource}, замеряющая время каждого SQL-запроса
     * (см. {@link SlowQueryDataSourceProxy}): все обычные потребители {@code DataSource} (JPA,
     * Liquibase, локи и т.д.) получают её, а код, которому нужен именно {@code HikariDataSource},
     * - оригинал.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "monitoring.slow.query.enabled", havingValue = "true")
    public DataSource slowQueryDataSource(
            HikariDataSource hikariDataSource,
            @Value("${monitoring.slow.query.threshold.milliseconds:3000}") long thresholdMillis) {
        log.info("Slow query data source monitoring enabled");
        return new SlowQueryDataSourceProxy(hikariDataSource, new SlowQueryLogger(thresholdMillis));
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
            HttpLoggingProperties httpLoggingProperties,
            ObjectProvider<RequestMappingHandlerMapping> handlerMappings) {
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
        return new FileKeyStoreObserver(keyStoreConfigs, properties.getDaysToExpire(), true, properties.getDaysExpired());
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
     * Настройки общего http-клиента - свойства {@code http.client.*}
     */
    @Bean
    @ConditionalOnProperty(name = "http.client.enabled", havingValue = "true")
    @ConfigurationProperties("http.client")
    public HttpClientProperties httpClientProperties() {
        return new HttpClientProperties();
    }

    /**
     * Фабрика билдеров http-клиентов. Если задана {@code http.client.keystore.location}, SSL-контекст
     * строится из этой ключницы (её сертификаты - и клиентские, и единственные доверенные), иначе
     * используется системный набор сертификатов JVM.
     */
    @Bean
    @ConditionalOnProperty(name = "http.client.enabled", havingValue = "true")
    public HttpClientBuilderFactory httpClientBuilderFactory(
            HttpClientProperties properties, ResourceLoader resourceLoader) {
        String location = properties.getKeystore().getLocation();
        if (location == null || location.isBlank()) {
            return HttpClientBuilderFactory.withDefaultSsl();
        }

        String password = properties.getKeystore().getPassword();
        KeyStore keyStore = new DefaultKeystoreFactory(location, password, resourceLoader)
                .createKeystoreBuilder()
                .includeAllLocalClientCertificates()
                .includeAllLocalTrustCertificates()
                .buildSilently()
                .build();
        SSLContext sslContext = SslContextUtils.keyStoreToSslContext(keyStore, password);
        return new HttpClientBuilderFactory(() -> sslContext, () -> null);
    }

    /**
     * Общий именованный http-клиент (закрывается вместе с контекстом).
     */
    @Bean
    @ConditionalOnProperty(name = "http.client.enabled", havingValue = "true")
    public CloseableHttpClient commonHttpClient(
            HttpClientBuilderFactory httpClientBuilderFactory, HttpClientProperties properties) {
        return commonHttpClientBuilder(httpClientBuilderFactory, properties).buildHttpClient();
    }

    /**
     * {@link RestTemplate} поверх {@link #commonHttpClient}: SSL из ключницы, таймауты, пул, retry при
     * потере соединения и логирование запросов/ответов на DEBUG. Именованные клиенты для отдельных
     * интеграций создаются так же: {@code httpClientBuilderFactory.createBuilder().withName("...").buildRestTemplate()}.
     */
    @Bean
    @ConditionalOnProperty(name = "http.client.enabled", havingValue = "true")
    public RestTemplate commonRestTemplate(
            HttpClientBuilderFactory httpClientBuilderFactory,
            HttpClientProperties properties,
            CloseableHttpClient commonHttpClient) {
        return commonHttpClientBuilder(httpClientBuilderFactory, properties).buildRestTemplate(commonHttpClient);
    }

    private static CommonHttpClientBuilder commonHttpClientBuilder(
            HttpClientBuilderFactory httpClientBuilderFactory, HttpClientProperties properties) {
        CommonHttpClientBuilder builder = httpClientBuilderFactory
                .createBuilder()
                .withName(properties.getName())
                .withConnectTimeout(properties.getConnectTimeoutMillis())
                .withReadTimeout(properties.getReadTimeoutMillis())
                .withConnectionPoolSize(properties.getConnectionPoolSize())
                .withVerifyHostname(properties.isVerifyHostname());
        if (properties.getRetryCount() > 0) {
            builder.withRetryCount(properties.getRetryCount());
        }
        return builder;
    }
}
