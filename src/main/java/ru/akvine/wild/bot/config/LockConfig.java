package ru.akvine.wild.bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import ru.akvine.commons.cluster.keepalive.KeepAliveService;
import ru.akvine.commons.cluster.lock.ConcurrentOperationsHelper;
import ru.akvine.commons.cluster.lock.DatabaseSLockProvider;
import ru.akvine.commons.cluster.lock.SLockProvider;

import javax.sql.DataSource;

@Configuration
public class LockConfig {
    @Value("${db.lock.check.interval.millis}")
    private long dbLockCheckIntervalMillis;
    @Value("${db.lock.expire.after.created.seconds}")
    private long dbLockExpireAfterCreatedSeconds;
    @Value("${db.lock.max.waiting.threads.per.lock}")
    private int dbLockMaxWaitingThreadsPerLock;

    @Bean(initMethod = "start", destroyMethod = "destroy")
    public SLockProvider lockProvider(DataSource dataSource, KeepAliveService keepAliveService, PlatformTransactionManager transactionManager) {
        return new DatabaseSLockProvider(dataSource, transactionManager, keepAliveService, dbLockCheckIntervalMillis, dbLockExpireAfterCreatedSeconds, dbLockMaxWaitingThreadsPerLock);
    }

    @Bean
    public ConcurrentOperationsHelper concurrentOperationsHelper(SLockProvider sLockProvider) {
        return new ConcurrentOperationsHelper(sLockProvider);
    }
}
