package ru.akvine.marketspace.bot.telegram;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TelegramClientStateResolver {
    private final static Map<String, String> clientStates = new ConcurrentHashMap<>();

    public void setState(String clientUuid, String state) {
        Preconditions.checkNotNull(clientUuid, "clientUuid is null");
        clientStates.put(clientUuid, state);
    }

    public boolean containsState(String clientUuid) {
        Preconditions.checkNotNull(clientUuid, "clientUuid is null");
        return clientStates.containsKey(clientUuid);
    }

    @Nullable
    public String getState(String clientUuid) {
        Preconditions.checkNotNull(clientUuid, "clientUuid is null");
        return clientStates.get(clientUuid);
    }

    public void removeState(String clientUuid) {
        Preconditions.checkNotNull(clientUuid, "clientUuid is null");
        clientStates.remove(clientUuid);
    }
}
