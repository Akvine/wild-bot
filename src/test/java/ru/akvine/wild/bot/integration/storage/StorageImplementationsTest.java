package ru.akvine.wild.bot.integration.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;
import ru.akvine.wild.bot.entities.infrastructure.IdempotencyKeyEntity;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorage;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorageInDatabaseImpl;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorageInMemoryImpl;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorageInRedisImpl;
import ru.akvine.wild.bot.infrastructure.idempotency.DatabaseIdempotencyStore;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyPayloadSerializer;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyRecord;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyStatus;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyStore;
import ru.akvine.wild.bot.infrastructure.idempotency.InMemoryIdempotencyStore;
import ru.akvine.wild.bot.infrastructure.idempotency.RedisIdempotencyStore;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.infrastructure.session.SessionStorageInDatabaseImpl;
import ru.akvine.wild.bot.infrastructure.session.SessionStorageInMemoryImpl;
import ru.akvine.wild.bot.infrastructure.session.SessionStorageInRedisImpl;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.infrastructure.state.StateStorageInDatabaseImpl;
import ru.akvine.wild.bot.infrastructure.state.StateStorageInMemoryImpl;
import ru.akvine.wild.bot.infrastructure.state.StateStorageInRedisImpl;
import ru.akvine.wild.bot.integration.base.BaseTest;
import ru.akvine.wild.bot.repositories.infrastructure.ClientSessionDataRepository;
import ru.akvine.wild.bot.repositories.infrastructure.ClientStatesRepository;
import ru.akvine.wild.bot.repositories.infrastructure.IdempotencyKeyRepository;
import ru.akvine.wild.bot.repositories.infrastructure.IterationCounterRepository;
import ru.akvine.wild.bot.services.AdvertService;
import ru.akvine.wild.bot.testsupport.FakeRedisOperationService;

/**
 * Все реализации одного интерфейса хранилища обязаны вести себя одинаково, поэтому каждый сценарий прогоняется по
 * реализациям в памяти, в БД (H2) и в Redis (Redis в памяти). Операции, зависящие от PostgreSQL
 * ({@code insert ... on conflict}), здесь не проверяются - они проверены на настоящем Postgres.
 */
@DisplayName("Хранилища состояний, сессий, счётчиков и идемпотентности: единый контракт для всех реализаций")
class StorageImplementationsTest extends BaseTest {
    private static final BotType BOT = BotType.TELEGRAM;

    @Autowired
    private ClientStatesRepository clientStatesRepository;

    @Autowired
    private ClientSessionDataRepository sessionRepository;

    @Autowired
    private IterationCounterRepository counterRepository;

    @Autowired
    private IdempotencyKeyRepository idempotencyRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private static String chat() {
        return "chat-" + UUID.randomUUID();
    }

    private Map<String, StateStorage<String, List<ClientState>>> stateStorages() {
        Map<String, StateStorage<String, List<ClientState>>> result = new LinkedHashMap<>();
        result.put("memory", new StateStorageInMemoryImpl());
        result.put("database", new StateStorageInDatabaseImpl(clientStatesRepository));
        result.put("redis", new StateStorageInRedisImpl(new FakeRedisOperationService<>()));
        return result;
    }

    @Test
    @DisplayName("Стек состояний: добавление, текущее, количество, возврат на предыдущее")
    void stateStackBasics() {
        stateStorages().forEach((name, storage) -> {
            String chat = chat();
            assertThat(storage.containsState(chat, BOT)).as(name).isFalse();
            assertThat(storage.statesCount(chat, BOT)).as(name).isZero();

            storage.add(chat, BOT, ClientState.MAIN_MENU);
            storage.add(chat, BOT, ClientState.TESTS_MENU);
            storage.add(chat, BOT, ClientState.WILDBERRIES_ACCOUNT_SETTINGS_MENU);

            assertThat(storage.containsState(chat, BOT)).as(name).isTrue();
            assertThat(storage.statesCount(chat, BOT)).as(name).isEqualTo(3);
            assertThat(storage.getCurrent(chat, BOT)).as(name).isEqualTo(ClientState.WILDBERRIES_ACCOUNT_SETTINGS_MENU);

            storage.removeCurrent(chat, BOT);
            assertThat(storage.getCurrent(chat, BOT)).as(name).isEqualTo(ClientState.TESTS_MENU);
            assertThat(storage.removeCurrentAndGetPrevious(chat, BOT)).as(name).isEqualTo(ClientState.MAIN_MENU);
        });
    }

    @Test
    @DisplayName("Стек состояний: откат к состоянию (backAt) обрезает историю, неизвестное состояние - false")
    void stateStackBackAt() {
        stateStorages().forEach((name, storage) -> {
            String chat = chat();
            storage.add(chat, BOT, ClientState.MAIN_MENU);
            storage.add(chat, BOT, ClientState.TESTS_MENU);
            storage.add(chat, BOT, ClientState.WILDBERRIES_ACCOUNT_SETTINGS_MENU);

            assertThat(storage.backAt(chat, BOT, ClientState.MAIN_MENU))
                    .as(name)
                    .isTrue();
            assertThat(storage.statesCount(chat, BOT)).as(name).isEqualTo(1);
            assertThat(storage.getCurrent(chat, BOT)).as(name).isEqualTo(ClientState.MAIN_MENU);
            assertThat(storage.backAt(chat, BOT, ClientState.TESTS_MENU))
                    .as(name)
                    .isFalse();
            assertThat(storage.backAt(chat(), BOT, ClientState.MAIN_MENU))
                    .as(name)
                    .isFalse();
        });
    }

    @Test
    @DisplayName("Стек состояний: close удаляет историю; операции над несуществующей историей - ошибка")
    void stateStackCloseAndMissing() {
        stateStorages().forEach((name, storage) -> {
            String chat = chat();
            storage.add(chat, BOT, ClientState.MAIN_MENU);

            storage.close(chat, BOT);
            assertThat(storage.containsState(chat, BOT)).as(name).isFalse();

            assertThatThrownBy(() -> storage.getCurrent(chat, BOT)).as(name).isInstanceOf(RuntimeException.class);
            assertThatThrownBy(() -> storage.removeCurrent(chat, BOT)).as(name).isInstanceOf(RuntimeException.class);
        });
    }

    private Map<String, SessionStorage<String, ClientSessionData>> sessionStorages() {
        Map<String, SessionStorage<String, ClientSessionData>> result = new LinkedHashMap<>();
        result.put("memory", new SessionStorageInMemoryImpl());
        result.put("database", new SessionStorageInDatabaseImpl(sessionRepository));
        result.put("redis", new SessionStorageInRedisImpl(new FakeRedisOperationService<>()));
        return result;
    }

    @Test
    @DisplayName("Сессия: init, чтение, сохранение изменённых данных, close")
    void sessionLifecycle() {
        sessionStorages().forEach((name, storage) -> {
            String chat = chat();
            assertThat(storage.hasSession(chat, BOT)).as(name).isFalse();

            storage.init(chat, BOT);
            assertThat(storage.hasSession(chat, BOT)).as(name).isTrue();

            ClientSessionData session = storage.get(chat, BOT);
            session.setSelectedCardType("Женский")
                    .setSelectedCategoryId(55)
                    .setNewCardPrice(1000)
                    .setNewCardDiscount(10)
                    .setAdvertIdToStart(777)
                    .setInputNewCardPriceAndDiscount(true)
                    .setUploadedCardPhoto(new byte[] {1, 2});
            storage.save(session, BOT);

            ClientSessionData reloaded = storage.get(chat, BOT);
            assertThat(reloaded.getSelectedCardType()).as(name).isEqualTo("Женский");
            assertThat(reloaded.getSelectedCategoryId()).as(name).isEqualTo(55);
            assertThat(reloaded.getNewCardPrice()).as(name).isEqualTo(1000);
            assertThat(reloaded.getAdvertIdToStart()).as(name).isEqualTo(777);
            assertThat(reloaded.isInputNewCardPriceAndDiscount()).as(name).isTrue();

            storage.close(chat, BOT);
            assertThat(storage.hasSession(chat, BOT)).as(name).isFalse();
        });
    }

    @Test
    @DisplayName("Сессия: чтение, сохранение и закрытие несуществующей сессии - ошибка")
    void sessionMissing() {
        sessionStorages().forEach((name, storage) -> {
            String chat = chat();
            assertThatThrownBy(() -> storage.get(chat, BOT)).as(name).isInstanceOf(RuntimeException.class);
            assertThatThrownBy(() -> storage.close(chat, BOT)).as(name).isInstanceOf(RuntimeException.class);
            assertThatThrownBy(() ->
                            storage.save(new ClientSessionData().setChatId(chat).setBotType(BOT), BOT))
                    .as(name)
                    .isInstanceOf(RuntimeException.class);
        });
    }

    private Map<String, CountersStorage> counterStorages() {
        AdvertService advertService = mock(AdvertService.class);
        CountersStorageInMemoryImpl memory = new CountersStorageInMemoryImpl(advertService);
        memory.init();
        Map<String, CountersStorage> result = new LinkedHashMap<>();
        result.put("memory", memory);
        result.put("database", new CountersStorageInDatabaseImpl(counterRepository));
        result.put("redis", new CountersStorageInRedisImpl(new FakeRedisOperationService<>()));
        return result;
    }

    private static int nextAdvertId() {
        return (int) (System.nanoTime() % 1_000_000_000);
    }

    @Test
    @DisplayName("Счётчик итераций: check срабатывает раз в N увеличений; после delete счётчика нет")
    void counters() {
        counterStorages().forEach((name, storage) -> {
            int advertId = nextAdvertId();
            storage.add(advertId);

            assertThat(storage.check(advertId, 3)).as(name).isTrue();
            storage.increase(advertId);
            assertThat(storage.check(advertId, 3)).as(name).isFalse();
            storage.increase(advertId);
            storage.increase(advertId);
            assertThat(storage.check(advertId, 3)).as(name).isTrue();

            storage.delete(advertId);
            assertThatThrownBy(() -> storage.increase(advertId)).as(name).isInstanceOf(RuntimeException.class);
            assertThatThrownBy(() -> storage.check(advertId, 3)).as(name).isInstanceOf(RuntimeException.class);
            assertThatThrownBy(() -> storage.delete(advertId)).as(name).isInstanceOf(RuntimeException.class);
        });
    }

    private Map<String, IdempotencyStore> idempotencyStores() {
        Map<String, IdempotencyStore> result = new LinkedHashMap<>();
        result.put("memory", new InMemoryIdempotencyStore());
        result.put(
                "redis",
                new RedisIdempotencyStore(new FakeRedisOperationService<>(), new IdempotencyPayloadSerializer()));
        return result;
    }

    @Test
    @DisplayName("Идемпотентность: begin занимает ключ один раз, complete сохраняет результат, release освобождает")
    void idempotencyLifecycle() {
        idempotencyStores().forEach((name, store) -> {
            String key = "key-" + UUID.randomUUID();

            assertThat(store.find(key)).as(name).isEmpty();
            assertThat(store.tryBegin(key, "fp", Duration.ofMinutes(5)))
                    .as(name)
                    .isTrue();
            assertThat(store.tryBegin(key, "fp", Duration.ofMinutes(5)))
                    .as(name)
                    .isFalse();
            assertThat(store.find(key))
                    .as(name)
                    .get()
                    .extracting(IdempotencyRecord::status)
                    .isEqualTo(IdempotencyStatus.IN_PROGRESS);

            store.complete(key, "{\"ok\":true}", Duration.ofHours(1));
            IdempotencyRecord completed = store.find(key).orElseThrow();
            assertThat(completed.status()).as(name).isEqualTo(IdempotencyStatus.COMPLETED);
            assertThat(completed.payload()).as(name).isEqualTo("{\"ok\":true}");
            assertThat(completed.fingerprint()).as(name).isEqualTo("fp");

            store.release(key);
            assertThat(store.find(key)).as(name).isEmpty();
            assertThat(store.tryBegin(key, "fp2", Duration.ofMinutes(5)))
                    .as(name)
                    .isTrue();
        });
    }

    @Test
    @DisplayName("Идемпотентность в БД: чтение, завершение, освобождение и очистка просроченных (без on conflict)")
    void databaseIdempotencyStore() {
        DatabaseIdempotencyStore store = new DatabaseIdempotencyStore(idempotencyRepository);
        String live = "live-" + UUID.randomUUID();
        String expired = "expired-" + UUID.randomUUID();
        idempotencyRepository.save(entity(live, java.time.LocalDateTime.now().plusMinutes(5)));
        idempotencyRepository.save(entity(expired, java.time.LocalDateTime.now().minusMinutes(5)));

        assertThat(store.find(live)).isPresent();
        assertThat(store.find(expired)).isEmpty();
        assertThat(store.find("missing")).isEmpty();

        store.complete(live, "payload", Duration.ofHours(1));
        IdempotencyRecord completed = store.find(live).orElseThrow();
        assertThat(completed.status()).isEqualTo(IdempotencyStatus.COMPLETED);
        assertThat(completed.payload()).isEqualTo("payload");
        store.complete("missing", "payload", Duration.ofHours(1));

        // store создан вручную (не через прокси), поэтому транзакцию открываем сами
        int deleted = transactionTemplate.execute(status -> store.deleteExpired());
        assertThat(deleted).isGreaterThanOrEqualTo(1);
        transactionTemplate.executeWithoutResult(status -> store.release(live));
        assertThat(store.find(live)).isEmpty();
    }

    private static IdempotencyKeyEntity entity(String key, java.time.LocalDateTime expiresAt) {
        return new IdempotencyKeyEntity()
                .setIdempotencyKey(key)
                .setFingerprint("fp")
                .setStatus(IdempotencyStatus.IN_PROGRESS)
                .setExpiresAt(expiresAt);
    }
}
