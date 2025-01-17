package ru.akvine.wild.bot.infrastructure.session;

import org.hibernate.SessionException;

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
    public ClientSessionData save(ClientSessionData data) {
        String chatId = data.getChatId();
        return sessions.replace(chatId, data);
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
