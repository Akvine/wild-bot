package ru.akvine.wild.bot.integration.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;
import ru.akvine.wild.bot.entities.infrastructure.OutboxMessageEntity;
import ru.akvine.wild.bot.infrastructure.outbox.DatabaseOutboxStore;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxMessage;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxStatus;
import ru.akvine.wild.bot.integration.base.BaseTest;
import ru.akvine.wild.bot.repositories.infrastructure.OutboxMessageRepository;

/**
 * Жизненный цикл сообщения outbox в БД: взять в обработку, отметить отправленным, повторить, провалить, вернуть
 * просроченные аренды и очистить отправленные. Вставка с дедупликацией ({@code on conflict}) - синтаксис PostgreSQL и
 * на H2 не работает, поэтому записи добавляются через репозиторий; сама вставка проверена на настоящем Postgres.
 */
@DisplayName("DatabaseOutboxStore: жизненный цикл сообщения")
class DatabaseOutboxStoreTest extends BaseTest {
    @Autowired
    private OutboxMessageRepository repository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private DatabaseOutboxStore store;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        store = new DatabaseOutboxStore(repository);
    }

    private OutboxMessageEntity pending(LocalDateTime nextAttemptAt) {
        return repository.save(new OutboxMessageEntity()
                .setMessageType("telegram")
                .setPayload("{\"text\":\"hi\"}")
                .setDedupKey("key-" + UUID.randomUUID())
                .setStatus(OutboxStatus.PENDING)
                .setAttempts(0)
                .setNextAttemptAt(nextAttemptAt));
    }

    private List<OutboxMessage> claim(int batch, Duration lease) {
        return transactionTemplate.execute(status -> store.claimBatch(batch, lease));
    }

    @Test
    @DisplayName("Взятие пачки: только созревшие сообщения, статус PROCESSING, аренда выставлена")
    void claimBatchTakesOnlyDueMessages() {
        OutboxMessageEntity due = pending(LocalDateTime.now().minusMinutes(1));
        OutboxMessageEntity future = pending(LocalDateTime.now().plusHours(1));

        List<OutboxMessage> claimed = claim(10, Duration.ofMinutes(5));

        assertThat(claimed).extracting(OutboxMessage::id).containsExactly(due.getId());
        assertThat(claimed.get(0).type()).isEqualTo("telegram");
        assertThat(claimed.get(0).attempts()).isZero();
        OutboxMessageEntity reloaded = repository.findById(due.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OutboxStatus.PROCESSING);
        assertThat(reloaded.getLockedUntil()).isAfter(LocalDateTime.now());
        assertThat(repository.findById(future.getId()).orElseThrow().getStatus())
                .isEqualTo(OutboxStatus.PENDING);
        assertThat(claim(10, Duration.ofMinutes(5))).isEmpty();
    }

    @Test
    @DisplayName("Размер пачки ограничивает число взятых сообщений")
    void claimBatchRespectsLimit() {
        pending(LocalDateTime.now().minusMinutes(3));
        pending(LocalDateTime.now().minusMinutes(2));
        pending(LocalDateTime.now().minusMinutes(1));

        assertThat(claim(2, Duration.ofMinutes(5))).hasSize(2);
        assertThat(claim(2, Duration.ofMinutes(5))).hasSize(1);
    }

    @Test
    @DisplayName("markSent фиксирует доставку и очищает ошибку и аренду")
    void markSent() {
        OutboxMessageEntity message = pending(LocalDateTime.now().minusMinutes(1));
        claim(1, Duration.ofMinutes(5));

        store.markSent(message.getId());

        OutboxMessageEntity reloaded = repository.findById(message.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OutboxStatus.SENT);
        assertThat(reloaded.getSentDate()).isNotNull();
        assertThat(reloaded.getLockedUntil()).isNull();
        assertThat(reloaded.getLastError()).isNull();
    }

    @Test
    @DisplayName("markRetry возвращает сообщение в очередь с новой попыткой, ошибка обрезается до 1000 символов")
    void markRetry() {
        OutboxMessageEntity message = pending(LocalDateTime.now().minusMinutes(1));
        claim(1, Duration.ofMinutes(5));
        LocalDateTime next = LocalDateTime.now().plusMinutes(10).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);

        store.markRetry(message.getId(), 2, next, "e".repeat(1500));

        OutboxMessageEntity reloaded = repository.findById(message.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(reloaded.getAttempts()).isEqualTo(2);
        assertThat(reloaded.getNextAttemptAt()).isEqualTo(next);
        assertThat(reloaded.getLockedUntil()).isNull();
        assertThat(reloaded.getLastError()).hasSize(1000);
    }

    @Test
    @DisplayName("markFailed помечает сообщение проваленным; ошибка null допустима")
    void markFailed() {
        OutboxMessageEntity message = pending(LocalDateTime.now().minusMinutes(1));

        store.markFailed(message.getId(), 5, null);

        OutboxMessageEntity reloaded = repository.findById(message.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(reloaded.getAttempts()).isEqualTo(5);
        assertThat(reloaded.getLastError()).isNull();
    }

    @Test
    @DisplayName("Просроченные аренды возвращаются в очередь, действующие не трогаются")
    void releaseExpiredLeases() {
        OutboxMessageEntity expired = pending(LocalDateTime.now().minusMinutes(1));
        OutboxMessageEntity active = pending(LocalDateTime.now().minusMinutes(1));
        expired.setStatus(OutboxStatus.PROCESSING)
                .setLockedUntil(LocalDateTime.now().minusMinutes(1));
        active.setStatus(OutboxStatus.PROCESSING)
                .setLockedUntil(LocalDateTime.now().plusMinutes(10));
        repository.saveAll(List.of(expired, active));

        int released = transactionTemplate.execute(status -> store.releaseExpiredLeases());

        assertThat(released).isEqualTo(1);
        assertThat(repository.findById(expired.getId()).orElseThrow().getStatus())
                .isEqualTo(OutboxStatus.PENDING);
        assertThat(repository.findById(active.getId()).orElseThrow().getStatus())
                .isEqualTo(OutboxStatus.PROCESSING);
    }

    @Test
    @DisplayName("Очистка удаляет только отправленные сообщения старше порога")
    void deleteSentBefore() {
        OutboxMessageEntity oldSent = pending(LocalDateTime.now().minusDays(10));
        oldSent.setStatus(OutboxStatus.SENT).setSentDate(LocalDateTime.now().minusDays(10));
        OutboxMessageEntity freshSent = pending(LocalDateTime.now().minusMinutes(1));
        freshSent.setStatus(OutboxStatus.SENT).setSentDate(LocalDateTime.now().minusMinutes(1));
        OutboxMessageEntity notSent = pending(LocalDateTime.now().minusDays(10));
        repository.saveAll(List.of(oldSent, freshSent, notSent));

        int deleted = transactionTemplate.execute(
                status -> store.deleteSentBefore(LocalDateTime.now().minusDays(1)));

        assertThat(deleted).isEqualTo(1);
        assertThat(repository.findById(oldSent.getId())).isEmpty();
        assertThat(repository.findById(freshSent.getId())).isPresent();
        assertThat(repository.findById(notSent.getId())).isPresent();
    }

    @Test
    @DisplayName("Операции над несуществующим сообщением - ошибка")
    void missingMessage() {
        assertThatThrownBy(() -> store.markSent(-1)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> store.markRetry(-1, 1, LocalDateTime.now(), "e"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> store.markFailed(-1, 1, "e")).isInstanceOf(IllegalStateException.class);
    }
}
