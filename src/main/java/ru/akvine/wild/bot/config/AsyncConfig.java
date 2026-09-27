package ru.akvine.wild.bot.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akvine.wild.bot.infrastructure.async.MdcThreadPoolExecutor;

@Configuration
public class AsyncConfig {

    @Bean
    public ExecutorService syncAdvertsExecutor(
            @Value("${sync.adverts.pool.size.core}") int poolSizeCore,
            @Value("${sync.adverts.keep.alive.seconds}") int seconds,
            @Value("${sync.adverts.queue.capacity}") int queueCapacity) {
        return new MdcThreadPoolExecutor(
                poolSizeCore,
                seconds,
                new LinkedBlockingQueue<>(queueCapacity),
                "sync-adverts",
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean
    public ExecutorService syncCardExecutor(
            @Value("${sync.card.pool.size.core}") int poolSizeCore,
            @Value("${sync.card.keep.alive.seconds}") int seconds,
            @Value("${sync.card.queue.capacity}") int queueCapacity) {
        return new MdcThreadPoolExecutor(
                poolSizeCore,
                seconds,
                new LinkedBlockingQueue<>(queueCapacity),
                "sync-card",
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean
    public ExecutorService syncCardTypeExecutor(
            @Value("${sync.card.type.pool.size.core}") int poolSizeCore,
            @Value("${sync.card.type.keep.alive.seconds}") int seconds,
            @Value("${sync.card.type.queue.capacity}") int queueCapacity) {
        return new MdcThreadPoolExecutor(
                poolSizeCore,
                seconds,
                new LinkedBlockingQueue<>(queueCapacity),
                "sync-card-type",
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean
    public ExecutorService messageExecutor(
            @Value("${message.pool.size.core}") int poolSizeCore,
            @Value("${message.keep.alive.seconds}") int seconds,
            @Value("${message.queue.capacity}") int queueCapacity) {
        return new MdcThreadPoolExecutor(
                poolSizeCore,
                seconds,
                new LinkedBlockingQueue<>(queueCapacity),
                "message",
                new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
