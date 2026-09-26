package ru.akvine.wild.bot.infrastructure.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.infrastructure.exceptions.NoSessionException;
import ru.akvine.wild.bot.services.integration.redis.RedisOperationService;

/**
 * Реализация {@link SessionStorage} поверх Redis ({@link RedisOperationService}): сессия хранится
 * в Redis целиком, переживает рестарт приложения и общая для всех инстансов. Ключи вида
 * {@code wild-bot:session:<chatId>_<botType>}; срока жизни у ключей нет - {@link #close} должен
 * вызываться по завершении сценария.
 * <p>
 * В отличие от {@link SessionStorageInMemoryImpl}, {@link #get} возвращает копию: изменения
 * сохраняются только после {@link #save}.
 */
@RequiredArgsConstructor
@Slf4j
public class SessionStorageInRedisImpl implements SessionStorage<String, ClientSessionData> {
    private static final String KEY_PREFIX = "wild-bot:session:";

    private final RedisOperationService<ClientSessionData> redisOperationService;

    @Override
    public void init(String chatId, BotType botType) {
        logger.debug("Init redis session for chat id = {} and bot type = {}", chatId, botType);
        ClientSessionData session = new ClientSessionData().setChatId(chatId).setBotType(botType);
        redisOperationService.putValue(key(chatId, botType), session);
    }

    @Override
    public ClientSessionData get(String chatId, BotType botType) {
        ClientSessionData session = redisOperationService.getValue(key(chatId, botType));
        if (session == null) {
            throw noSession(chatId, botType);
        }
        return session;
    }

    @Override
    public ClientSessionData save(ClientSessionData data, BotType botType) {
        String chatId = data.getChatId();
        if (chatId == null) {
            throw new IllegalArgumentException("Can't save session without chat id");
        }
        if (!hasSession(chatId, botType)) {
            throw noSession(chatId, botType);
        }

        data.setBotType(botType);
        redisOperationService.putValue(key(chatId, botType), data);
        return data;
    }

    @Override
    public boolean hasSession(String chatId, BotType botType) {
        return redisOperationService.hasKey(key(chatId, botType));
    }

    @Override
    public void close(String chatId, BotType botType) {
        if (!hasSession(chatId, botType)) {
            throw noSession(chatId, botType);
        }
        redisOperationService.delete(key(chatId, botType));
    }

    private NoSessionException noSession(String chatId, BotType botType) {
        return new NoSessionException("Has no session for identifier " + chatId + " and bot type = " + botType);
    }

    private static String key(String chatId, BotType botType) {
        return KEY_PREFIX + chatId + "_" + botType;
    }
}
