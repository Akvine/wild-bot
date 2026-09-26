package ru.akvine.wild.bot.entities.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.entities.base.BaseEntity;
import ru.akvine.wild.bot.infrastructure.idempotency.IdempotencyStatus;

/**
 * Ключ идемпотентности ({@link ru.akvine.wild.bot.infrastructure.idempotency.DatabaseIdempotencyStore}).
 * Сам ключ - первичный ключ таблицы, поэтому две одновременные вставки одного ключа невозможны.
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Table(name = "IDEMPOTENCY_KEY_ENTITY")
@Entity
public class IdempotencyKeyEntity extends BaseEntity {
    @Id
    @Column(name = "IDEMPOTENCY_KEY", updatable = false, nullable = false)
    private String idempotencyKey;

    @Column(name = "FINGERPRINT")
    private String fingerprint;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private IdempotencyStatus status;

    @Column(name = "PAYLOAD")
    private String payload;

    @Column(name = "EXPIRES_AT", nullable = false)
    private LocalDateTime expiresAt;
}
