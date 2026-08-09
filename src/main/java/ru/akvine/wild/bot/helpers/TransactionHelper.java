package ru.akvine.wild.bot.helpers;

import java.util.concurrent.Callable;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/***
 * Утилитный класс для выполнения логики в транзакции
 * Если ставим локи, то использовать эти методы нужно строго после взятия локов, а не до!!!
 */
@Component
public class TransactionHelper {
    @SneakyThrows
    @Transactional
    public <R> R execute(Callable<R> callable) {
        return callable.call();
    }

    @SneakyThrows
    @Transactional
    public void execute(Runnable runnable) {
        runnable.run();
    }

    @SneakyThrows
    @Transactional(readOnly = true)
    public <R> R executeReadOnly(Callable<R> callable) {
        return callable.call();
    }

    @SneakyThrows
    @Transactional(propagation = Propagation.NEVER)
    public <R> R executeWithoutTransaction(Callable<R> callable) {
        return callable.call();
    }

    @SneakyThrows
    @Transactional(propagation = Propagation.NEVER)
    public void executeWithoutTransaction(Runnable runnable) {
        runnable.run();
    }
}
