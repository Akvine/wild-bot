package ru.akvine.wild.bot.services.integration.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RDeque;
import org.redisson.api.RKeys;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

@DisplayName("RedisOperationService: операции делегируются нужным структурам Redisson")
@SuppressWarnings({"unchecked", "rawtypes"})
class RedisOperationServiceTest {
    private RedissonClient redisson;
    private RedisOperationService<String> service;

    @BeforeEach
    void setUp() {
        redisson = mock(RedissonClient.class);
        service = new RedisOperationServiceBuilder<String>(redisson).build();
    }

    @Test
    @DisplayName("Хэш-таблица: put, размер, putIfAbsent, putAll, удаление ключей")
    void mapOperations() {
        RMap map = mock(RMap.class);
        when(redisson.getMap("map")).thenReturn(map);
        when(map.size()).thenReturn(3);
        when(map.putIfAbsent("k", "v")).thenReturn(null);
        when(map.putIfAbsent("exists", "v")).thenReturn("old");

        service.putMap("map", "k", "v");
        assertThat(service.getMapSize("map")).isEqualTo(3);
        assertThat(service.putIfAbsent("map", "k", "v")).isTrue();
        assertThat(service.putIfAbsent("map", "exists", "v")).isFalse();
        service.putAll("map", Map.of("a", "1"));
        service.deleteKeysInHashTable("map", "a", "b");

        verify(map).put("k", "v");
        verify(map).putAll(Map.of("a", "1"));
        verify(map).remove("a");
        verify(map).remove("b");
    }

    @Test
    @DisplayName("Хэш-таблица: случайные значения, чтение по ключам и целиком")
    void mapReads() {
        RMap map = mock(RMap.class);
        when(redisson.getMap("map")).thenReturn(map);
        when(map.randomEntries(2)).thenReturn(Map.of("a", "1", "b", "2"));
        when(map.randomEntries(1)).thenReturn(Map.of("a", "1")).thenReturn(Map.of());
        when(map.getAll(any(Set.class))).thenReturn(Map.of("a", "1"));
        when(map.readAllMap()).thenReturn(Map.of("a", "1"));

        assertThat(service.randomEntries("map", 2)).hasSize(2);
        assertThat(service.randomEntry("map")).isNotNull();
        assertThat(service.randomEntry("map")).isNull();
        assertThat(service.getListValuesByHashKeys("map", (Collection) List.of("a")))
                .containsExactly("1");
        assertThat(service.getMapAsAll("map")).containsEntry("a", "1");
    }

    @Test
    @DisplayName("Ключи: существование, удаление и удаление по префиксу")
    void keyOperations() {
        RKeys keys = mock(RKeys.class);
        when(redisson.getKeys()).thenReturn(keys);
        when(keys.countExists("present")).thenReturn(1L);
        when(keys.countExists("absent")).thenReturn(0L);

        assertThat(service.hasKey("present")).isTrue();
        assertThat(service.hasKey("absent")).isFalse();
        service.delete("a", "b");
        service.deleteByPrefix("prefix:");

        verify(keys).delete("a", "b");
        verify(keys).deleteByPattern("prefix:*");
    }

    @Test
    @DisplayName("Список и очередь: добавление, чтение, размер, peek/poll, trim")
    void listOperations() {
        RList list = mock(RList.class);
        RDeque deque = mock(RDeque.class);
        when(redisson.getList("list")).thenReturn(list);
        when(redisson.getDeque("list")).thenReturn(deque);
        when(list.readAll()).thenReturn(List.of("a", "b"));
        when(list.size()).thenReturn(2);
        when(deque.peekLast()).thenReturn("b");
        when(deque.pollLast()).thenReturn("b");

        service.addValueInList("list", "a");
        assertThat(service.getListValues("list")).containsExactly("a", "b");
        assertThat(service.getListSize("list")).isEqualTo(2);
        assertThat(service.peekLastInList("list")).isEqualTo("b");
        assertThat(service.pollLastInList("list")).isEqualTo("b");
        service.trimList("list", 0, 5);

        verify(list).add("a");
        verify(list).trim(0, 5);
    }

    @Test
    @DisplayName("Значения: set, set с TTL, setIfAbsent с TTL, get")
    void bucketOperations() {
        RBucket bucket = mock(RBucket.class);
        when(redisson.getBucket("key")).thenReturn(bucket);
        when(bucket.get()).thenReturn("value");
        when(bucket.setIfAbsent("v", Duration.ofSeconds(5))).thenReturn(true);

        service.putValue("key", "v");
        service.putValueWithTtl("key", "v", Duration.ofSeconds(1));
        assertThat(service.putValueIfAbsentWithTtl("key", "v", Duration.ofSeconds(5)))
                .isTrue();
        assertThat(service.getValue("key")).isEqualTo("value");

        verify(bucket).set("v");
        verify(bucket).set("v", Duration.ofSeconds(1));
    }

    @Test
    @DisplayName("Атомарный счётчик: сохранить, увеличить, прочитать")
    void atomicLongOperations() {
        RAtomicLong counter = mock(RAtomicLong.class);
        when(redisson.getAtomicLong("counter")).thenReturn(counter);
        when(counter.incrementAndGet()).thenReturn(8L);
        when(counter.get()).thenReturn(7L);

        service.saveChunkValue("counter", 7L);
        assertThat(service.incrementAndGetChunkValue("counter")).isEqualTo(8L);
        assertThat(service.getChunkValue("counter")).isEqualTo(7L);

        verify(counter).set(7L);
    }
}
