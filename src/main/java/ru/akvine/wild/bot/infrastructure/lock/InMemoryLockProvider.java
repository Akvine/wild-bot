package ru.akvine.wild.bot.infrastructure.lock;

/**
 * Именованная блокировка по строковому ключу. Название "in-memory" отражает то, что состояние
 * лока не обязано переживать рестарт инстанса, но конкретная реализация может быть как
 * локальной для одного инстанса ({@link InstanceLockProvider}), так и распределённой между
 * несколькими инстансами приложения ({@code RedisInMemoryLockProvider}).
 */
public interface InMemoryLockProvider {
    /**
     * Захватывает блокировку по ключу, блокируя текущий поток до её освобождения.
     *
     * @param key ключ блокировки
     */
    void lock(String key);

    /**
     * Пытается захватить блокировку по ключу без ожидания.
     *
     * @param key ключ блокировки
     * @return {@code true}, если блокировка захвачена
     */
    boolean tryLock(String key);

    /**
     * Освобождает ранее захваченную блокировку по ключу.
     *
     * @param key ключ блокировки
     */
    void unlock(String key);
}
