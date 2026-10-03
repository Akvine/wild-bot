package ru.akvine.wild.bot.testsupport;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.akvine.wild.bot.services.integration.redis.RedisOperationService;

/**
 * Redis в памяти: переопределяет только операции, которые нужны хранилищам состояний, сессий, счётчиков и
 * идемпотентности. Позволяет проверять Redis-реализации тем же набором тестов, что и остальные, без сервера Redis.
 */
public class FakeRedisOperationService<T> extends RedisOperationService<T> {
    private final Map<String, Object> values = new HashMap<>();
    private final Map<String, List<Object>> lists = new HashMap<>();
    private final Map<String, Long> counters = new HashMap<>();

    public FakeRedisOperationService() {
        super(null);
    }

    @Override
    public boolean hasKey(String redisKey) {
        return values.containsKey(redisKey) || lists.containsKey(redisKey) || counters.containsKey(redisKey);
    }

    @Override
    public void addValueInList(String listKey, T data) {
        lists.computeIfAbsent(listKey, key -> new ArrayList<>()).add(data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> getListValues(String listKey) {
        return new ArrayList<>((List<T>) lists.getOrDefault(listKey, List.of()));
    }

    @Override
    public int getListSize(String listKey) {
        return lists.getOrDefault(listKey, List.of()).size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T peekLastInList(String listKey) {
        List<Object> list = lists.get(listKey);
        return list == null || list.isEmpty() ? null : (T) list.get(list.size() - 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T pollLastInList(String listKey) {
        List<Object> list = lists.get(listKey);
        if (list == null || list.isEmpty()) {
            return null;
        }
        T last = (T) list.remove(list.size() - 1);
        if (list.isEmpty()) {
            lists.remove(listKey);
        }
        return last;
    }

    @Override
    public void trimList(String listKey, int fromIndex, int toIndex) {
        List<Object> list = lists.get(listKey);
        if (list != null) {
            lists.put(listKey, new ArrayList<>(list.subList(fromIndex, Math.min(toIndex + 1, list.size()))));
        }
    }

    @Override
    public void putValue(String key, T value) {
        values.put(key, value);
    }

    @Override
    public void putValueWithTtl(String key, T value, Duration ttl) {
        values.put(key, value);
    }

    @Override
    public boolean putValueIfAbsentWithTtl(String key, T value, Duration ttl) {
        return values.putIfAbsent(key, value) == null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getValue(String key) {
        return (T) values.get(key);
    }

    @Override
    public void delete(String... keys) {
        for (String key : keys) {
            values.remove(key);
            lists.remove(key);
            counters.remove(key);
        }
    }

    @Override
    public void saveChunkValue(String key, Long count) {
        counters.put(key, count);
    }

    @Override
    public Long incrementAndGetChunkValue(String key) {
        return counters.merge(key, 1L, Long::sum);
    }

    @Override
    public Long getChunkValue(String key) {
        return counters.get(key);
    }
}
