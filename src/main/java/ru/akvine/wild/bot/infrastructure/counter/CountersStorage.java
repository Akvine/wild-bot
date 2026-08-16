package ru.akvine.wild.bot.infrastructure.counter;

/**
 * Хранилище счётчиков итераций для запущенных рекламных кампаний — используется, чтобы
 * не проверять/увеличивать бюджет и CPM кампании на каждой итерации фоновой проверки,
 * а только раз в {@code maxCountBeforeIncrease} итераций (см. {@link #check}). Есть две
 * реализации: {@link CountersStorageInMemoryImpl} (быстрая, теряется при рестарте) и
 * {@link CountersStorageInDatabaseImpl} (переживает рестарт приложения).
 */
public interface CountersStorage {
    int ZERO_COUNT_INIT = 0;

    /**
     * Заводит новый счётчик для кампании с нулевым значением.
     *
     * @param advertId внешний id рекламной кампании
     */
    void add(int advertId);

    /**
     * Увеличивает счётчик кампании на 1.
     *
     * @param advertId внешний id рекламной кампании
     */
    void increase(int advertId);

    /**
     * Удаляет счётчик кампании.
     *
     * @param advertId внешний id рекламной кампании
     */
    void delete(int advertId);

    /**
     * Проверяет, наступила ли для кампании очередная контрольная итерация.
     *
     * @param advertId              внешний id рекламной кампании
     * @param maxCountBeforeIncrease раз в сколько итераций наступает контрольная точка
     * @return {@code true}, если текущее значение счётчика кратно {@code maxCountBeforeIncrease}
     */
    boolean check(int advertId, int maxCountBeforeIncrease);
}
