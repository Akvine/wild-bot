package ru.akvine.wild.bot.infrastructure.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.exceptions.NoStateException;

/**
 * Реализация {@link StateStorage} поверх {@link ConcurrentHashMap} в памяти процесса: для
 * каждого chatId хранится список состояний как стек (последний элемент — текущее состояние),
 * что и обеспечивает работу кнопки "Назад". Не переживает рестарт приложения и не видна
 * другим инстансам при горизонтальном масштабировании.
 */
@Slf4j
public class StateStorageInMemoryImpl implements StateStorage<String, List<ClientState>> {
    private static final Map<String, List<ClientState>> STATES = new ConcurrentHashMap<>();

    @Override
    public void add(String chatId, BotType botType, ClientState state) {
        if (!containsState(chatId, botType)) {
            STATES.put(createUniqueIdentifier(chatId, botType), new ArrayList<>(Arrays.asList(state)));
        } else {
            STATES.get(createUniqueIdentifier(chatId, botType)).add(state);
        }
    }

    @Override
    public boolean containsState(String chatId, BotType botType) {
        return STATES.containsKey(createUniqueIdentifier(chatId, botType));
    }

    @Override
    public ClientState getCurrent(String chatId, BotType botType) {
        validate(chatId, botType);
        return STATES.get(createUniqueIdentifier(chatId, botType)).getLast();
    }

    @Override
    public void removeCurrent(String chatId, BotType botType) {
        validate(chatId, botType);
        STATES.get(createUniqueIdentifier(chatId, botType)).removeLast();
    }

    @Override
    public ClientState removeCurrentAndGetPrevious(String chatId, BotType botType) {
        removeCurrent(chatId, botType);
        return getCurrent(chatId, botType);
    }

    @Override
    public void close(String chatId, BotType botType) {
        validate(chatId, botType);
        STATES.remove(createUniqueIdentifier(chatId, botType));
    }

    @Override
    public int statesCount(String chatId, BotType botType) {
        return STATES.get(createUniqueIdentifier(chatId, botType)).size();
    }

    private void validate(String chatId, BotType botType) {
        if (!containsState(chatId, botType)) {
            throw new NoStateException("No state for identifier = [" + chatId + "] and bot type = [" + botType + "]");
        }
    }

    private String createUniqueIdentifier(String chatId, BotType botType) {
        return chatId + "_" + botType;
    }
}
