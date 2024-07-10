package ru.akvine.marketspace.bot.infrastructure;

public interface SessionStorage<ID, R> {
    void init(ID identifier);

    R get(ID identifier);

    boolean hasSession(ID identifier);

    void close(ID identifier);
}
