package ru.akvine.wild.bot.config;

import com.zaxxer.hikari.HikariDataSource;
import java.time.Duration;
import java.util.List;
import javax.sql.DataSource;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.support.TransactionTemplate;
import ru.akvine.commons.cluster.lock.ConcurrentOperationsHelper;
import ru.akvine.commons.cluster.lock.SLockProvider;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorage;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorageInDatabaseImpl;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorageInMemoryImpl;
import ru.akvine.wild.bot.infrastructure.lock.DistributedLockProvider;
import ru.akvine.wild.bot.infrastructure.lock.distributed.DataBaseLockProvider;
import ru.akvine.wild.bot.infrastructure.lock.distributed.RedisLockProvider;
import ru.akvine.wild.bot.infrastructure.monitoring.SlowQueryDataSourceProxy;
import ru.akvine.wild.bot.infrastructure.monitoring.SlowQueryLogger;
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
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.infrastructure.state.StateStorageInDatabaseImpl;
import ru.akvine.wild.bot.infrastructure.state.StateStorageInMemoryImpl;
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
    @ConditionalOnProperty(name = "counter.storage.implementation.type", havingValue = "database")
    public CountersStorage databaseIterationsStorage(IterationCounterRepository iterationCounterRepository) {
        return new CountersStorageInDatabaseImpl(iterationCounterRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "retry.executor.implementation.type", havingValue = "exponential")
    public RetryExecutor exponentialRetryExecutor(
            @Value("${send.file.retry.attempts.count}") int attempts,
            @Value("${send.file.retry.initial.delay.millis}") int retryInitialDelayMillis,
            @Value("${send.file.retry.exponential.backoff.multiplier}") double retryExponentialBackoffMultiplier,
            @Value("${send.file.retry.max.delay.millis}") int retryMaxDelayMillis) {

        return new ExponentialRetryExecutor(
                attempts,
                Duration.ofMillis(retryInitialDelayMillis),
                retryExponentialBackoffMultiplier,
                Duration.ofMillis(retryMaxDelayMillis));
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
}
