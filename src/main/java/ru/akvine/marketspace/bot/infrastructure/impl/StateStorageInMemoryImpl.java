package ru.akvine.marketspace.bot.infrastructure.impl;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.infrastructure.StateStorage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StateStorageInMemoryImpl implements StateStorage<String> {
    private final static Map<String, ClientState> states = new ConcurrentHashMap<>();

    public void setState(String chatId, ClientState state) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        states.put(chatId, state);
    }

    public boolean containsState(String chatId) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        return states.containsKey(chatId);
    }

    @Nullable
    public ClientState getState(String chatId) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        return states.get(chatId);
    }

    public void removeState(String chatId) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        states.remove(chatId);
    }
}
