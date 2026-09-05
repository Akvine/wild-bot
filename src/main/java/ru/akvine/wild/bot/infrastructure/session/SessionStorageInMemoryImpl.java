package ru.akvine.wild.bot.infrastructure.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.SessionException;
import ru.akvine.wild.bot.enums.BotType;

/**
 * Реализация {@link SessionStorage} поверх {@link ConcurrentHashMap} в памяти процесса —
 * быстрее {@link SessionStorageInDatabaseImpl}, но не переживает рестарт приложения и не
 * видна другим инстансам при горизонтальном масштабировании.
 */
public class SessionStorageInMemoryImpl implements SessionStorage<String, ClientSessionData> {
    private final Map<String, ClientSessionData> sessions = new ConcurrentHashMap<>();

    @Override
    public void init(String chatId, BotType botType) {
        sessions.put(createUniqueIdentifier(chatId, botType), new ClientSessionData());
    }

    @Override
    public ClientSessionData get(String chatId, BotType botType) {
        validate(chatId, botType);
        return sessions.get(createUniqueIdentifier(chatId, botType));
    }

    @Override
    public ClientSessionData save(ClientSessionData data, BotType botType) {
        String chatId = data.getChatId();
        return sessions.replace(createUniqueIdentifier(chatId, botType), data);
    }

    @Override
    public boolean hasSession(String chatId, BotType botType) {
        return sessions.containsKey(createUniqueIdentifier(chatId, botType));
    }

    @Override
    public void close(String chatId, BotType botType) {
        validate(chatId, botType);
        sessions.remove(createUniqueIdentifier(chatId, botType));
    }

    private void validate(String chatId, BotType botType) {
        if (!hasSession(chatId, botType)) {
            throw new SessionException("Has no session for identifier " + chatId + " and bot type = " + botType);
        }
    }

    private static String createUniqueIdentifier(String chatId, BotType botType) {
        return chatId + "_" + botType;
    }
}
