package ru.akvine.marketspace.bot.infrastructure.state;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import ru.akvine.marketspace.bot.enums.ClientState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class StateStorageInMemoryImpl implements StateStorage<String> {
    private final static Map<String, ClientState> states = new ConcurrentHashMap<>();

    public void setState(String chatId, ClientState state) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        logger.debug("Set state = {} for chat with id = {}", state, chatId);
        states.put(chatId, state);
    }

    public boolean containsState(String chatId) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        logger.debug("Check chat id = {} has state", chatId);
        return states.containsKey(chatId);
    }

    @Nullable
    public ClientState getState(String chatId) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        logger.debug("Get state for chat id = {}", chatId);
        return states.get(chatId);
    }

    public void removeState(String chatId) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        logger.debug("Remove state for chat id = {}", chatId);
        states.remove(chatId);
    }
}
