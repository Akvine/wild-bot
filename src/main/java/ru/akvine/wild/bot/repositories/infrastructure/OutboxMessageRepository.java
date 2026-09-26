package ru.akvine.wild.bot.repositories.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.wild.bot.entities.infrastructure.OutboxMessageEntity;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxStatus;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessageEntity, Long> {

    /**
     * Вставка с дедупликацией: сообщение с уже существующим {@code dedupKey} не добавляется, и в отличие
     * от обычного {@code save} конфликт не ломает транзакцию исключением (иначе откатилось бы и само
     * бизнес-изменение). Для {@code dedupKey = null} дедупликации нет.
     *
     * @return 1, если сообщение добавлено, 0, если такой ключ уже был
     */
    @Modifying
    @Query(
            value = "insert into OUTBOX_MESSAGE_ENTITY (ID, MESSAGE_TYPE, PAYLOAD, DEDUP_KEY, STATUS, ATTEMPTS, "
                    + "NEXT_ATTEMPT_AT, CREATED_DATE) "
                    + "values (nextval('SEQ_OUTBOX_MESSAGE_ENTITY'), :type, :payload, cast(:dedupKey as varchar(255)), 'PENDING', 0, "
                    + ":now, :now) "
                    + "on conflict (DEDUP_KEY) do nothing",
            nativeQuery = true)
    int insertIfAbsent(
            @Param("type") String type,
            @Param("payload") String payload,
            @Param("dedupKey") String dedupKey,
            @Param("now") LocalDateTime now);

    /**
     * Сообщения, которым пора отправляться. Блокирует строки и пропускает уже заблокированные другими
     * инстансами ({@code skip locked}), поэтому одно сообщение не достанется двум relay'ям.
     */
    @Query(
            value = "select * from OUTBOX_MESSAGE_ENTITY where STATUS = 'PENDING' and NEXT_ATTEMPT_AT <= :now "
                    + "order by ID limit :limit for update skip locked",
            nativeQuery = true)
    List<OutboxMessageEntity> findDueForUpdate(@Param("now") LocalDateTime now, @Param("limit") int limit);

    @Modifying
    @Query("update OutboxMessageEntity m set m.status = :pending, m.lockedUntil = null, m.updatedDate = :now "
            + "where m.status = :processing and m.lockedUntil < :now")
    int releaseExpiredLeases(
            @Param("now") LocalDateTime now,
            @Param("pending") OutboxStatus pending,
            @Param("processing") OutboxStatus processing);

    @Modifying
    @Query("delete from OutboxMessageEntity m where m.status = :status and m.sentDate < :before")
    int deleteByStatusAndSentBefore(@Param("status") OutboxStatus status, @Param("before") LocalDateTime before);
}
