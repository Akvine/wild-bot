package ru.akvine.wild.bot.unit.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxHandler;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxMessage;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxProperties;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxRelay;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxService;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxStatus;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxStore;
import ru.akvine.wild.bot.services.integration.BotIntegrationAdapter;
import ru.akvine.wild.bot.services.outbox.BotMessageOutbox;
import ru.akvine.wild.bot.services.outbox.BotMessagePayload;

class OutboxTest {

    /** Хранилище в памяти с теми же правилами, что у БД-реализации: дедупликация, аренда, время следующей попытки */
    static class FakeOutboxStore implements OutboxStore {
        static class Row {
            long id;
            String type;
            String payload;
            String dedupKey;
            OutboxStatus status = OutboxStatus.PENDING;
            int attempts;
            LocalDateTime nextAttemptAt = LocalDateTime.now().minusSeconds(1);
            LocalDateTime lockedUntil;
            String lastError;
        }

        final List<Row> rows = new ArrayList<>();
        int releaseCalls;

        @Override
        public boolean add(String type, String payload, String dedupKey) {
            if (dedupKey != null && rows.stream().anyMatch(row -> dedupKey.equals(row.dedupKey))) {
                return false;
            }
            Row row = new Row();
            row.id = rows.size() + 1;
            row.type = type;
            row.payload = payload;
            row.dedupKey = dedupKey;
            rows.add(row);
            return true;
        }

        @Override
        public List<OutboxMessage> claimBatch(int batchSize, Duration lease) {
            LocalDateTime now = LocalDateTime.now();
            List<OutboxMessage> batch = new ArrayList<>();
            for (Row row : rows) {
                if (batch.size() < batchSize && row.status == OutboxStatus.PENDING && !row.nextAttemptAt.isAfter(now)) {
                    row.status = OutboxStatus.PROCESSING;
                    row.lockedUntil = now.plus(lease);
                    batch.add(new OutboxMessage(row.id, row.type, row.payload, row.dedupKey, row.attempts));
                }
            }
            return batch;
        }

        @Override
        public int releaseExpiredLeases() {
            releaseCalls++;
            LocalDateTime now = LocalDateTime.now();
            int released = 0;
            for (Row row : rows) {
                if (row.status == OutboxStatus.PROCESSING && row.lockedUntil.isBefore(now)) {
                    row.status = OutboxStatus.PENDING;
                    row.lockedUntil = null;
                    released++;
                }
            }
            return released;
        }

        @Override
        public void markSent(long id) {
            row(id).status = OutboxStatus.SENT;
        }

        @Override
        public void markRetry(long id, int attempts, LocalDateTime nextAttemptAt, String error) {
            Row row = row(id);
            row.status = OutboxStatus.PENDING;
            row.attempts = attempts;
            row.nextAttemptAt = nextAttemptAt;
            row.lastError = error;
        }

        @Override
        public void markFailed(long id, int attempts, String error) {
            Row row = row(id);
            row.status = OutboxStatus.FAILED;
            row.attempts = attempts;
            row.lastError = error;
        }

        @Override
        public int deleteSentBefore(LocalDateTime before) {
            return 0;
        }

        Row row(long id) {
            return rows.get((int) id - 1);
        }
    }

    private final FakeOutboxStore store = new FakeOutboxStore();
    private final OutboxProperties properties = new OutboxProperties();
    private final OutboxService outboxService = new OutboxService(store);

    private OutboxHandler handler(String type, AtomicInteger calls, boolean failing) {
        return new OutboxHandler() {
            @Override
            public String type() {
                return type;
            }

            @Override
            public void handle(OutboxMessage message) {
                calls.incrementAndGet();
                if (failing) {
                    throw new IllegalStateException("Telegram is down");
                }
            }
        };
    }

    private OutboxRelay relay(OutboxHandler... handlers) {
        return new OutboxRelay(store, List.of(handlers), properties);
    }

    @Test
    @DisplayName("Записанное сообщение доставляется relay'ем ровно один раз и получает статус SENT")
    void messageIsDeliveredOnce() {
        AtomicInteger calls = new AtomicInteger();
        outboxService.enqueue("T", new BotMessagePayload("1", BotType.TELEGRAM, "hi"), null);
        OutboxRelay relay = relay(handler("T", calls, false));

        assertThat(relay.process()).isEqualTo(1);
        assertThat(relay.process()).isZero();

        assertThat(calls).hasValue(1);
        assertThat(store.row(1).status).isEqualTo(OutboxStatus.SENT);
    }

    @Test
    @DisplayName("Сбой доставки откладывает сообщение с паузой: сразу повторно оно не берётся")
    void failedDeliveryIsRetriedLater() {
        AtomicInteger calls = new AtomicInteger();
        outboxService.enqueue("T", new BotMessagePayload("1", BotType.TELEGRAM, "hi"), null);
        OutboxRelay relay = relay(handler("T", calls, true));

        relay.process();
        relay.process();

        assertThat(calls).hasValue(1);
        FakeOutboxStore.Row row = store.row(1);
        assertThat(row.status).isEqualTo(OutboxStatus.PENDING);
        assertThat(row.attempts).isEqualTo(1);
        assertThat(row.lastError).contains("Telegram is down");
        assertThat(row.nextAttemptAt).isAfter(LocalDateTime.now().plusSeconds(5));
    }

    @Test
    @DisplayName("После maxAttempts неудач сообщение получает статус FAILED и больше не берётся")
    void messageFailsAfterMaxAttempts() {
        properties.setMaxAttempts(3);
        AtomicInteger calls = new AtomicInteger();
        outboxService.enqueue("T", new BotMessagePayload("1", BotType.TELEGRAM, "hi"), null);
        OutboxRelay relay = relay(handler("T", calls, true));

        for (int i = 0; i < 5; i++) {
            store.row(1).nextAttemptAt = LocalDateTime.now().minusSeconds(1); // «прошла пауза»
            relay.process();
        }

        assertThat(calls).hasValue(3);
        assertThat(store.row(1).status).isEqualTo(OutboxStatus.FAILED);
        assertThat(store.row(1).attempts).isEqualTo(3);
    }

    @Test
    @DisplayName("Сообщение неизвестного типа сразу помечается FAILED")
    void unknownTypeIsFailed() {
        outboxService.enqueue("UNKNOWN", new BotMessagePayload("1", BotType.TELEGRAM, "hi"), null);

        relay(handler("T", new AtomicInteger(), false)).process();

        assertThat(store.row(1).status).isEqualTo(OutboxStatus.FAILED);
        assertThat(store.row(1).lastError).contains("UNKNOWN");
    }

    @Test
    @DisplayName("Сбойное сообщение не мешает доставке остальных в пачке")
    void oneFailureDoesNotBlockOthers() {
        AtomicInteger failingCalls = new AtomicInteger();
        AtomicInteger okCalls = new AtomicInteger();
        outboxService.enqueue("BAD", new BotMessagePayload("1", BotType.TELEGRAM, "a"), null);
        outboxService.enqueue("OK", new BotMessagePayload("2", BotType.TELEGRAM, "b"), null);

        relay(handler("BAD", failingCalls, true), handler("OK", okCalls, false)).process();

        assertThat(failingCalls).hasValue(1);
        assertThat(okCalls).hasValue(1);
        assertThat(store.row(2).status).isEqualTo(OutboxStatus.SENT);
    }

    @Test
    @DisplayName("Каждый проход возвращает в очередь сообщения с истёкшей арендой (relay упал), и они доставляются")
    void expiredLeasesAreReturnedToQueue() {
        AtomicInteger calls = new AtomicInteger();
        outboxService.enqueue("T", new BotMessagePayload("1", BotType.TELEGRAM, "hi"), null);
        // relay взял сообщение и упал: оно осталось в PROCESSING с истёкшей арендой
        store.claimBatch(10, Duration.ofSeconds(-1));
        assertThat(store.row(1).status).isEqualTo(OutboxStatus.PROCESSING);

        relay(handler("T", calls, false)).process();

        assertThat(store.releaseCalls).isEqualTo(1);
        assertThat(calls).hasValue(1);
        assertThat(store.row(1).status).isEqualTo(OutboxStatus.SENT);
    }

    @Test
    @DisplayName("Два обработчика одного типа - ошибка конфигурации")
    void duplicateHandlersAreRejected() {
        assertThatThrownBy(
                        () -> relay(handler("T", new AtomicInteger(), false), handler("T", new AtomicInteger(), false)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Пауза между попытками удваивается и ограничена максимумом")
    void backoffDoublesAndIsCapped() {
        properties.setInitialBackoffSeconds(10);
        properties.setMaxBackoffSeconds(100);

        assertThat(properties.backoff(1)).isEqualTo(Duration.ofSeconds(10));
        assertThat(properties.backoff(2)).isEqualTo(Duration.ofSeconds(20));
        assertThat(properties.backoff(3)).isEqualTo(Duration.ofSeconds(40));
        assertThat(properties.backoff(5)).isEqualTo(Duration.ofSeconds(100));
        assertThat(properties.backoff(50)).isEqualTo(Duration.ofSeconds(100));
    }

    @Test
    @DisplayName("Сообщение с существующим ключом дедупликации не добавляется второй раз")
    void duplicateDedupKeyIsSkipped() {
        BotMessagePayload payload = new BotMessagePayload("1", BotType.TELEGRAM, "hi");

        boolean first = outboxService.enqueue("T", payload, "subscription-expiring:42");
        boolean second = outboxService.enqueue("T", payload, "subscription-expiring:42");
        boolean withoutKey = outboxService.enqueue("T", payload, null);
        boolean withoutKeyAgain = outboxService.enqueue("T", payload, null);

        assertThat(first).isTrue();
        assertThat(second).isFalse();
        assertThat(withoutKey).isTrue();
        assertThat(withoutKeyAgain).isTrue();
        assertThat(store.rows).hasSize(3);
    }

    @Test
    @DisplayName("Содержимое сообщения сериализуется в JSON и читается обратно")
    void payloadRoundTrips() {
        outboxService.enqueue("T", new BotMessagePayload("100", BotType.MAX, "Привет"), null);
        OutboxMessage message = new OutboxMessage(1, "T", store.row(1).payload, null, 0);

        BotMessagePayload payload = outboxService.read(message, BotMessagePayload.class);

        assertThat(store.row(1).payload).contains("\"chatId\":\"100\"");
        assertThat(payload).isEqualTo(new BotMessagePayload("100", BotType.MAX, "Привет"));
    }

    @Test
    @DisplayName("BotMessageOutbox: ставит сообщение в очередь и доставляет его через адаптер бота")
    void botMessageOutboxEnqueuesAndSends() {
        BotIntegrationAdapter adapter = mock(BotIntegrationAdapter.class);
        BotMessageOutbox botMessageOutbox = new BotMessageOutbox(outboxService, adapter);
        OutboxRelay relay = relay(botMessageOutbox);

        boolean enqueued = botMessageOutbox.enqueue("100", BotType.TELEGRAM, "Подписка заканчивается", "key-1");
        relay.process();

        assertThat(enqueued).isTrue();
        assertThat(store.rows.stream().map(row -> row.type).collect(Collectors.toList()))
                .containsExactly(BotMessageOutbox.TYPE);
        verify(adapter).sendMessage("100", BotType.TELEGRAM, "Подписка заканчивается");
        assertThat(store.row(1).status).isEqualTo(OutboxStatus.SENT);
    }
}
