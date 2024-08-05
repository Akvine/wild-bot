package ru.akvine.marketspace.bot.infrastructure.session;

public interface SessionStorage<ID, R> {
    void init(ID identifier);

    R get(ID identifier);

    R save(R data);

    boolean hasSession(ID identifier);

    void close(ID identifier);
}
