package ru.akvine.wild.bot.entities.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.entities.base.BaseEntity;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxStatus;

/**
 * Сообщение outbox ({@link ru.akvine.wild.bot.infrastructure.outbox.DatabaseOutboxStore}). Добавляется
 * нативной вставкой (для дедупликации без исключения), поэтому последовательность идентификаторов
 * с шагом 1 общая для JPA и нативного SQL.
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Table(name = "OUTBOX_MESSAGE_ENTITY")
@Entity
public class OutboxMessageEntity extends BaseEntity {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outboxMessageEntitySeq")
    @SequenceGenerator(name = "outboxMessageEntitySeq", sequenceName = "SEQ_OUTBOX_MESSAGE_ENTITY", allocationSize = 1)
    private Long id;

    @Column(name = "MESSAGE_TYPE", nullable = false, updatable = false)
    private String messageType;

    @Column(name = "PAYLOAD", nullable = false, updatable = false)
    private String payload;

    @Column(name = "DEDUP_KEY", updatable = false)
    private String dedupKey;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    @Column(name = "ATTEMPTS", nullable = false)
    private int attempts;

    @Column(name = "NEXT_ATTEMPT_AT", nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(name = "LOCKED_UNTIL")
    private LocalDateTime lockedUntil;

    @Column(name = "LAST_ERROR")
    private String lastError;

    @Column(name = "SENT_DATE")
    private LocalDateTime sentDate;
}
