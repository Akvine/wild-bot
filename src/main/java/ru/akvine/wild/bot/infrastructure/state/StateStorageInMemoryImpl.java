package ru.akvine.wild.bot.infrastructure.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.exceptions.NoStateException;

@Slf4j
public class StateStorageInMemoryImpl implements StateStorage<String, List<ClientState>> {
    private static final Map<String, List<ClientState>> STATES = new ConcurrentHashMap<>();

    @Override
    public void add(String chatId, ClientState state) {
        if (!containsState(chatId)) {
            STATES.put(chatId, new ArrayList<>(Arrays.asList(state)));
        } else {
            STATES.get(chatId).add(state);
        }
    }

    @Override
    public boolean containsState(String chatId) {
        return STATES.containsKey(chatId);
    }

    @Override
    public ClientState getCurrent(String chatId) {
        validate(chatId);
        return STATES.get(chatId).getLast();
    }

    @Override
    public void removeCurrent(String chatId) {
        validate(chatId);
        STATES.get(chatId).removeLast();
    }

    @Override
    public ClientState removeCurrentAndGetPrevious(String chatId) {
        removeCurrent(chatId);
        return getCurrent(chatId);
    }

    @Override
    public void close(String chatId) {
        validate(chatId);
        STATES.remove(chatId);
    }

    @Override
    public int statesCount(String chatId) {
        return STATES.get(chatId).size();
    }

    private void validate(String chatId) {
        if (!containsState(chatId)) {
            throw new NoStateException("No state for identifier = [" + chatId + "]");
        }
    }
}
