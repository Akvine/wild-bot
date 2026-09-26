package ru.akvine.wild.bot.infrastructure.outbox;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.akvine.wild.bot.entities.infrastructure.OutboxMessageEntity;
import ru.akvine.wild.bot.repositories.infrastructure.OutboxMessageRepository;

/**
 * Outbox в БД ({@code OUTBOX_MESSAGE_ENTITY}). {@link #add} обязательно участвует в транзакции вызывающего
 * кода ({@code MANDATORY}), а операции relay'я - отдельные короткие транзакции ({@code REQUIRES_NEW}).
 */
@RequiredArgsConstructor
public class DatabaseOutboxStore implements OutboxStore {
    private static final int MAX_ERROR_LENGTH = 1000;

    private final OutboxMessageRepository repository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean add(String type, String payload, String dedupKey) {
        return repository.insertIfAbsent(type, payload, dedupKey, LocalDateTime.now()) == 1;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<OutboxMessage> claimBatch(int batchSize, Duration lease) {
        LocalDateTime now = LocalDateTime.now();
        List<OutboxMessageEntity> due = repository.findDueForUpdate(now, batchSize);
        due.forEach(entity -> {
            entity.setStatus(OutboxStatus.PROCESSING);
            entity.setLockedUntil(now.plus(lease));
            entity.setUpdatedDate(now);
        });
        repository.saveAll(due);
        return due.stream()
                .map(entity -> new OutboxMessage(
                        entity.getId(),
                        entity.getMessageType(),
                        entity.getPayload(),
                        entity.getDedupKey(),
                        entity.getAttempts()))
                .toList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int releaseExpiredLeases() {
        return repository.releaseExpiredLeases(LocalDateTime.now(), OutboxStatus.PENDING, OutboxStatus.PROCESSING);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSent(long id) {
        OutboxMessageEntity entity = verifyExists(id);
        LocalDateTime now = LocalDateTime.now();
        entity.setStatus(OutboxStatus.SENT);
        entity.setLockedUntil(null);
        entity.setLastError(null);
        entity.setSentDate(now);
        entity.setUpdatedDate(now);
        repository.save(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRetry(long id, int attempts, LocalDateTime nextAttemptAt, String error) {
        OutboxMessageEntity entity = verifyExists(id);
        entity.setStatus(OutboxStatus.PENDING);
        entity.setAttempts(attempts);
        entity.setNextAttemptAt(nextAttemptAt);
        entity.setLockedUntil(null);
        entity.setLastError(truncate(error));
        entity.setUpdatedDate(LocalDateTime.now());
        repository.save(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(long id, int attempts, String error) {
        OutboxMessageEntity entity = verifyExists(id);
        entity.setStatus(OutboxStatus.FAILED);
        entity.setAttempts(attempts);
        entity.setLockedUntil(null);
        entity.setLastError(truncate(error));
        entity.setUpdatedDate(LocalDateTime.now());
        repository.save(entity);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int deleteSentBefore(LocalDateTime before) {
        return repository.deleteByStatusAndSentBefore(OutboxStatus.SENT, before);
    }

    private OutboxMessageEntity verifyExists(long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new IllegalStateException("Outbox message with id = [" + id + "] not found"));
    }

    private static String truncate(String error) {
        if (error == null || error.length() <= MAX_ERROR_LENGTH) {
            return error;
        }
        return error.substring(0, MAX_ERROR_LENGTH);
    }
}
