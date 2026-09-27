package ru.akvine.wild.bot.infrastructure.async;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.MDC;
import ru.akvine.wild.bot.config.AsyncConfig;

/**
 * {@link ThreadPoolExecutor}, который переносит MDC (username, chatId, botType) из потока, отправившего задачу, в
 * рабочий поток: иначе логи задач в пуле были бы без этих полей. Используется в {@link AsyncConfig} для всех пулов
 * ручного распараллеливания через {@link java.util.concurrent.CompletableFuture}.
 */
public class MdcThreadPoolExecutor extends ThreadPoolExecutor {

    public MdcThreadPoolExecutor(
            int poolSize,
            long keepAliveSeconds,
            BlockingQueue<Runnable> queue,
            String threadNamePrefix,
            RejectedExecutionHandler rejectedExecutionHandler) {
        super(
                poolSize,
                poolSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                queue,
                threadFactory(threadNamePrefix),
                rejectedExecutionHandler);
    }

    private static ThreadFactory threadFactory(String threadNamePrefix) {
        AtomicInteger counter = new AtomicInteger();
        return runnable -> new Thread(runnable, threadNamePrefix + "-" + counter.incrementAndGet());
    }

    @Override
    public void execute(Runnable task) {
        Map<String, String> callerMdc = MDC.getCopyOfContextMap();
        super.execute(() -> {
            if (callerMdc != null) {
                MDC.setContextMap(callerMdc);
            }
            try {
                task.run();
            } finally {
                MDC.clear();
            }
        });
    }
}
