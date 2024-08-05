package ru.akvine.marketspace.bot.infrastructure.state;

import org.jetbrains.annotations.Nullable;
import ru.akvine.marketspace.bot.enums.ClientState;

public interface StateStorage<T> {
    void setState(T identifier, ClientState state);

    boolean containsState(T identifier);

    @Nullable
    ClientState getState(T identifier);

    void removeState(T identifier);
}
