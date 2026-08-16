package ru.akvine.wild.bot.infrastructure.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.SessionException;

/**
 * Реализация {@link SessionStorage} поверх {@link ConcurrentHashMap} в памяти процесса —
 * быстрее {@link SessionStorageInDatabaseImpl}, но не переживает рестарт приложения и не
 * видна другим инстансам при горизонтальном масштабировании.
 */
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
