package ru.akvine.wild.bot.infrastructure.state;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.exceptions.NoStateException;
import ru.akvine.wild.bot.services.integration.redis.RedisOperationService;

/**
 * Реализация {@link StateStorage} поверх Redis ({@link RedisOperationService}): стек состояний
 * клиента - список в Redis, последний элемент - текущее состояние. Переживает рестарт
 * приложения и общий для всех инстансов. Добавление и снятие состояния со стека выполняются
 * атомарными командами Redis. Ключи вида {@code wild-bot:states:<chatId>_<botType>}; срока жизни
 * у ключей нет - {@link #close} должен вызываться по завершении сценария.
 */
@RequiredArgsConstructor
@Slf4j
public class StateStorageInRedisImpl implements StateStorage<String, List<ClientState>> {
    private static final String KEY_PREFIX = "wild-bot:states:";

    private final RedisOperationService<ClientState> redisOperationService;

    @Override
    public void add(String chatId, BotType botType, ClientState state) {
        redisOperationService.addValueInList(key(chatId, botType), state);
    }

    @Override
    public boolean containsState(String chatId, BotType botType) {
        return redisOperationService.hasKey(key(chatId, botType));
    }

    @Override
    public ClientState getCurrent(String chatId, BotType botType) {
        ClientState current = redisOperationService.peekLastInList(key(chatId, botType));
        if (current == null) {
            throw noState(chatId, botType);
        }
        return current;
    }

    @Override
    public void removeCurrent(String chatId, BotType botType) {
        if (redisOperationService.pollLastInList(key(chatId, botType)) == null) {
            throw noState(chatId, botType);
        }
    }

    @Override
    public ClientState removeCurrentAndGetPrevious(String chatId, BotType botType) {
        removeCurrent(chatId, botType);
        return getCurrent(chatId, botType);
    }

    @Override
    public boolean backAt(String chatId, BotType botType, ClientState targetClientState) {
        String key = key(chatId, botType);
        List<ClientState> states = redisOperationService.getListValues(key);
        if (states.isEmpty()) {
            throw noState(chatId, botType);
        }

        int targetIndex = states.indexOf(targetClientState);
        if (targetIndex < 0) {
            return false;
        }

        redisOperationService.trimList(key, 0, targetIndex);
        return true;
    }

    @Override
    public void close(String chatId, BotType botType) {
        if (!containsState(chatId, botType)) {
            throw noState(chatId, botType);
        }
        redisOperationService.delete(key(chatId, botType));
    }

    @Override
    public int statesCount(String chatId, BotType botType) {
        return redisOperationService.getListSize(key(chatId, botType));
    }

    private NoStateException noState(String chatId, BotType botType) {
        return new NoStateException("No state for identifier = [" + chatId + "] and bot type = [" + botType + "]");
    }

    private static String key(String chatId, BotType botType) {
        return KEY_PREFIX + chatId + "_" + botType;
    }
}
