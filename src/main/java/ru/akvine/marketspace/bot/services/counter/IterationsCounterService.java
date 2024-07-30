package ru.akvine.marketspace.bot.services.counter;

public interface IterationsCounterService {
    int ZERO_COUNT_INIT = 0;

    void add(int advertId);

    void increase(int advertId);

    void delete(int advertId);

    boolean check(int advertId, int maxCountBeforeIncrease);
}
