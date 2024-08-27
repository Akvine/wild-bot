package ru.akvine.marketspace.bot.infrastructure.state;

import lombok.extern.slf4j.Slf4j;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.infrastructure.exceptions.NoStateException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class StateStorageInMemoryImpl implements StateStorage<String, List<ClientState>> {
    private final static Map<String, List<ClientState>> states = new ConcurrentHashMap<>();

    @Override
    public void add(String chatId, ClientState state) {
        if (!containsState(chatId)) {
            states.put(chatId, new ArrayList<>(Arrays.asList(state)));
        } else {
            states.get(chatId).add(state);
        }
    }

    @Override
    public boolean containsState(String chatId) {
        return states.containsKey(chatId);
    }

    @Override
    public ClientState getCurrent(String chatId) {
        validate(chatId);
        return states.get(chatId).getLast();
    }

    @Override
    public void removeCurrent(String chatId) {
        validate(chatId);
        states.get(chatId).removeLast();
    }

    @Override
    public ClientState removeCurrentAndGetPrevious(String chatId) {
        removeCurrent(chatId);
        return getCurrent(chatId);
    }

    @Override
    public void close(String chatId) {
        validate(chatId);
        states.remove(chatId);
    }

    @Override
    public int statesCount(String chatId) {
        return states.get(chatId).size();
    }

    private void validate(String chatId) {
        if (!containsState(chatId)) {
            throw new NoStateException("No state for identifier = [" + chatId + "]");
        }
    }
}
