package ru.akvine.wild.bot.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.enums.BotType;

@Entity
@Table(name = "CLIENT_BLOCKED_CREDENTIALS_ENTITY")
@Getter
@Setter
@Accessors(chain = true)
public class ClientBlockedCredentialsEntity {
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clientBlockedCredentialsEntitySequence")
    @SequenceGenerator(
            name = "clientBlockedCredentialsEntitySequence",
            sequenceName = "SEQ_CLIENT_BLOCKED_CREDENTIALS_ENTITY",
            allocationSize = 1000)
    private Long id;

    @Column(name = "CHAT_ID", nullable = false)
    private String chatId;

    @Column(name = "BOT_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private BotType botType;

    @Column(name = "BLOCK_START_DATE", nullable = false)
    private LocalDateTime blockStartDate;

    @Column(name = "BLOCK_END_DATE", nullable = false)
    private LocalDateTime blockEndDate;
}
