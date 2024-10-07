package ru.akvine.wild.bot.services.integration.redis;

import org.redisson.api.*;

import java.util.*;

/**
 * Сервис для работы с Redis кэшом
 * Определены основные операции со структурами данных в Redis кэше
 *
 *
 * @param <T> Кэшируемый объект
 */
public class RedisOperationService<T> {
    private final RedissonClient redisson;

    public RedisOperationService(RedissonClient redisson) {
        this.redisson = redisson;
    }

    /**
     * Дбавляет объект в хэш-таблицу
     */
    public void putMap(String redisKey, Object key, T data) {
        RMap<Object, T> map = redisson.getMap(redisKey);
        map.put(key, data);
    }

    public int getMapSize(String redisKey) {
        return redisson.getMap(redisKey).size();
    }

    /**
     * Добавляет объект в хэш-таблицу, если его там не существовало до этого
     */
    public boolean putIfAbsent(String redisKey, Object key, T data) {
        RMap<Object, T> map = redisson.getMap(redisKey);
        return map.putIfAbsent(key, data) == null;
    }

    /**
     * Добавляет объекты в хэш-таблицу
     */
    public void putAll(String redisKey, Map<Object, T> data) {
        RMap<Object, T> map = redisson.getMap(redisKey);
        map.putAll(data);
    }

    /**
     * Проверяет существование ключа
     */
    public boolean hasKey(String redisKey) {
        RKeys keys = redisson.getKeys();
        return keys.countExists(redisKey) != 0;
    }

    /**
     * Удаляет из хэш-таблицы значения по списку ключей
     */
    public void deleteKeysInHashTable(String redisKey, String... hashKeys) {
        RMap<Object, T> map = redisson.getMap(redisKey);
        Arrays.stream(hashKeys).forEach(map::remove);
    }

    /**
     * Возвращает указанное кол-во случайных значений из хэш-таблицы
     */
    public Map<Object, T> randomEntries(String cacheKey, int count) {
        RMap<Object, T> map = redisson.getMap(cacheKey);
        return map.randomEntries(count);
    }

    /**
     * Возвращает случайный объект из хэш-таблицы
     */
    public Map.Entry<Object, T> randomEntry(String cacheKey) {
        RMap<Object, T> map = redisson.getMap(cacheKey);
        Iterator<Map.Entry<Object, T>> iterator = map.randomEntries(1).entrySet().iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * Возвращает список объектов по массиву ключей
     */
    public List<T> getListValuesByHashKeys(String redisKey, Collection<Object> hashKeys) {
        RMap<Object, T> map = redisson.getMap(redisKey);
        return new ArrayList<>(map.getAll(new HashSet<>(hashKeys)).values());
    }

    /**
     * Возвращает все объекты из хэш-таблицы
     */
    public Map<Object, T> getMapAsAll(String redisKey) {
        RMap<Object, T> map = redisson.getMap(redisKey);
        return map.readAllMap();
    }

    /**
     * Сохраняет элементы в порядке вставки
     */

    public void addValueInList(String listKey, T data) {
        RList<Object> list = redisson.getList(listKey);
        list.add(data);
    }

    /**
     * Возвращает все элементы списка
     */
    public List<T> getListValues(String listKey) {
        RList<T> list = redisson.getList(listKey);
        return list.readAll();
    }

    /**
     * Сохраняет объект
     */
    public void putValue(String key, T value) {
        RBucket<Object> bucket = redisson.getBucket(key);
        bucket.set(value);
    }

    /**
     * Возвращает объект из кэша
     */
    public T getValue(String key) {
        RBucket<T> bucket = redisson.getBucket(key);
        return bucket.get();
    }

    /**
     * Удаляет объект
     */
    public void delete(String... key) {
        RKeys keys = redisson.getKeys();
        keys.delete(key);
    }

    /**
     * УДаляет объекты с ключами, начинающимися на указанный префикс
     */
    public void deleteByPrefix(String keyPrefix) {
        RKeys keys = redisson.getKeys();
        keys.deleteByPattern(keyPrefix + "*");
    }

    public void saveChunkValue(String key, Long count) {
        RAtomicLong atomicLong = redisson.getAtomicLong(key);
        atomicLong.set(count);
    }

    public Long incrementAndGetChunkValue(String key) {
        RAtomicLong atomicLong = redisson.getAtomicLong(key);
        return atomicLong.incrementAndGet();
    }

    public Long getChunkValue(String key) {
        RAtomicLong atomicLong = redisson.getAtomicLong(key);
        return atomicLong.get();
    }
}
