package ru.akvine.wild.bot.infrastructure.state;

import ru.akvine.wild.bot.enums.ClientState;

public interface StateStorage<T, R> {
    void add(T identifier, ClientState state);

    boolean containsState(T identifier);

    ClientState getCurrent(T identifier);

    void removeCurrent(T identifier);

    ClientState removeCurrentAndGetPrevious(T identifier);

    void close(T identifier);

    int statesCount(T identifier);
}
