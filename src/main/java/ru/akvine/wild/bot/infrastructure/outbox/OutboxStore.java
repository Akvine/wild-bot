package ru.akvine.wild.bot.infrastructure.outbox;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Хранилище outbox. Сообщение записывается в той же транзакции БД, что и бизнес-изменение
 * ({@link #add}), поэтому оно появляется тогда и только тогда, когда изменение сохранено.
 */
public interface OutboxStore {

    /**
     * Добавляет сообщение в очередь. Обязано вызываться внутри транзакции бизнес-изменения: без неё
     * пропадает смысл паттерна, поэтому реализация бросает исключение, если транзакции нет.
     *
     * @param dedupKey ключ дедупликации: сообщение с уже существующим ключом не добавляется; {@code null} - без дедупликации
     * @return {@code true}, если сообщение добавлено, {@code false}, если такой ключ уже был
     */
    boolean add(String type, String payload, String dedupKey);

    /**
     * Атомарно забирает до {@code batchSize} сообщений, которым пора отправляться, и закрепляет их за
     * вызывающим на {@code lease}: одно сообщение не достанется двум инстансам.
     */
    List<OutboxMessage> claimBatch(int batchSize, Duration lease);

    /**
     * Возвращает в очередь сообщения, аренда которых истекла (relay, взявший их, упал)
     *
     * @return сколько сообщений возвращено
     */
    int releaseExpiredLeases();

    void markSent(long id);

    /**
     * Откладывает сообщение до {@code nextAttemptAt}
     */
    void markRetry(long id, int attempts, LocalDateTime nextAttemptAt, String error);

    /**
     * Помечает сообщение недоставляемым: больше попыток не будет
     */
    void markFailed(long id, int attempts, String error);

    /**
     * Удаляет доставленные сообщения старше {@code before}
     *
     * @return сколько удалено
     */
    int deleteSentBefore(LocalDateTime before);
}
