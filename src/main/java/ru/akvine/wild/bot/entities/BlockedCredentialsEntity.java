package ru.akvine.wild.bot.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Entity
@Table(name = "BLOCKED_CREDENTIALS_ENTITY")
@Getter
@Setter
@Accessors(chain = true)
public class BlockedCredentialsEntity {
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "blockedCredentialsEntitySequence")
    @SequenceGenerator(name = "blockedCredentialsEntitySequence", sequenceName = "SEQ_BLOCKED_CREDENTIALS_ENTITY", allocationSize = 1000)
    private Long id;

    @Column(name = "UUID", nullable = false)
    private String uuid;

    @Column(name = "BLOCK_START_DATE", nullable = false)
    private LocalDateTime blockStartDate;

    @Column(name = "BLOCK_END_DATE", nullable = false)
    private LocalDateTime blockEndDate;
}
