package ru.akvine.wild.bot.infrastructure.lock;

public interface InMemoryLockProvider {
    void lock(String key);

    boolean tryLock(String key);

    void unlock(String key);
}
