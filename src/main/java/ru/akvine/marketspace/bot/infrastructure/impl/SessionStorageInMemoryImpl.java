package ru.akvine.marketspace.bot.infrastructure.impl;

import org.hibernate.SessionException;
import ru.akvine.marketspace.bot.infrastructure.SessionStorage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionStorageInMemoryImpl implements SessionStorage<String, ClientSessionData> {
    private final Map<String, ClientSessionData> sessions = new ConcurrentHashMap<>();

    @Override
    public void init(String chatId) {
        sessions.put(chatId, new ClientSessionData());
    }

    @Override
    public ClientSessionData get(String chatId) {
        validate(chatId);
        return sessions.get(chatId);
    }

    @Override
    public boolean hasSession(String chatId) {
        return sessions.containsKey(chatId);
    }

    @Override
    public void close(String chatId) {
        validate(chatId);
        sessions.remove(chatId);
    }

    private void validate(String chatId) {
        if (!hasSession(chatId)) {
            throw new SessionException("Has no session for identifier " + chatId);
        }
    }
}
