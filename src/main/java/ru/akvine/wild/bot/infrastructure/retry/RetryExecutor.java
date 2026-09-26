package ru.akvine.wild.bot.infrastructure.retry;

import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Исполнитель задач с автоматическим повтором при исключениях.
 * <p>
 * Реализации определяют число попыток (всего, включая первую) и задержку между ними. Контракт
 * одинаков для всех реализаций:
 * <ul>
 *     <li>успешная задача выполняется ровно один раз - на первой удачной попытке;</li>
 *     <li>исключение, которое не проходит {@code retryOn}, не повторяется: {@link RuntimeException}
 *     пробрасывается как есть, проверяемое оборачивается в
 *     {@link ru.akvine.wild.bot.exceptions.RetryException};</li>
 *     <li>если все попытки исчерпаны, выбрасывается
 *     {@link ru.akvine.wild.bot.exceptions.RetryException} с переданным сообщением, причиной которого
 *     служит последняя ошибка задачи;</li>
 *     <li>если поток прерван, повторы прекращаются, флаг прерывания восстанавливается, выбрасывается
 *     {@link ru.akvine.wild.bot.exceptions.RetryException}.</li>
 * </ul>
 * Задержка между попытками блокирует вызывающий поток. Повторять стоит только идемпотентные
 * операции: если запрос дошёл до сервера, а ответ потерялся, повтор выполнит его второй раз.
 */
public interface RetryExecutor {

    /** Повторять при любом исключении */
    Predicate<Exception> RETRY_ON_ANY = exception -> true;

    /**
     * Выполняет задачу с повторами; основной метод, остальные - удобные обёртки над ним.
     *
     * @param task         задача, может бросать проверяемые исключения
     * @param retryOn      определяет, стоит ли повторять после данного исключения
     * @param errorMessage сообщение для {@code RetryException} и логов, что именно не удалось
     */
    <T> T call(Callable<T> task, Predicate<? super Exception> retryOn, String errorMessage);

    default <T> T execute(Supplier<T> task, String errorMessage) {
        return execute(task, RETRY_ON_ANY, errorMessage);
    }

    default <T> T execute(Supplier<T> task, Predicate<? super Exception> retryOn, String errorMessage) {
        return call(task::get, retryOn, errorMessage);
    }

    default void execute(Runnable task, String errorMessage) {
        execute(task, RETRY_ON_ANY, errorMessage);
    }

    default void execute(Runnable task, Predicate<? super Exception> retryOn, String errorMessage) {
        call(
                () -> {
                    task.run();
                    return null;
                },
                retryOn,
                errorMessage);
    }
}
