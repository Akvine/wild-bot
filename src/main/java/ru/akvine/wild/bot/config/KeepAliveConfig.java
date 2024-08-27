package ru.akvine.wild.bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.commons.cluster.keepalive.KeepAliveService;

import javax.sql.DataSource;

@Configuration
public class KeepAliveConfig {
    @Value("${db.keepalive.interval.seconds}")
    private long dbKeepAliveIntervalSeconds;
    @Value("${db.keepalive.delay.coefficient}")
    private int dbKeepAliveDelayCoefficient;

    @Bean(initMethod = "start", destroyMethod = "destroy")
    public KeepAliveService keepAliveService(DataSource dataSource) {
        return new KeepAliveService(dataSource, dbKeepAliveIntervalSeconds, dbKeepAliveDelayCoefficient);
    }
}
