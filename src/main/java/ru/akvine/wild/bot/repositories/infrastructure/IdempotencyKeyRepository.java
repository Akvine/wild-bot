package ru.akvine.wild.bot.repositories.infrastructure;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.wild.bot.entities.infrastructure.IdempotencyKeyEntity;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, String> {

    /**
     * Атомарная вставка: если ключ уже есть, ничего не происходит (в отличие от обычного
     * {@code save}, конфликт не портит транзакцию исключением)
     *
     * @return 1, если запись создана, 0, если ключ уже был
     */
    @Modifying
    @Query(
            value =
                    "insert into IDEMPOTENCY_KEY_ENTITY (IDEMPOTENCY_KEY, FINGERPRINT, STATUS, EXPIRES_AT, CREATED_DATE) "
                            + "values (:key, :fingerprint, 'IN_PROGRESS', :expiresAt, :now) "
                            + "on conflict (IDEMPOTENCY_KEY) do nothing",
            nativeQuery = true)
    int insertIfAbsent(
            @Param("key") String key,
            @Param("fingerprint") String fingerprint,
            @Param("expiresAt") LocalDateTime expiresAt,
            @Param("now") LocalDateTime now);

    @Modifying
    @Query("delete from IdempotencyKeyEntity e where e.idempotencyKey = :key and e.expiresAt <= :now")
    int deleteExpiredByKey(@Param("key") String key, @Param("now") LocalDateTime now);

    @Modifying
    @Query("delete from IdempotencyKeyEntity e where e.idempotencyKey = :key")
    int deleteByKey(@Param("key") String key);

    @Modifying
    @Query("delete from IdempotencyKeyEntity e where e.expiresAt <= :now")
    int deleteExpired(@Param("now") LocalDateTime now);
}
