package ru.akvine.wild.bot.entities.security;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.enums.security.ActionState;

@Entity
@Table(name = "ACCESS_RESTORE_ACTION_ENTITY")
@Getter
@Setter
@Accessors(chain = true)
public class AccessRestoreActionEntity implements OneTimePasswordable {
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accessRestoreEntitySequence")
    @SequenceGenerator(name = "accessRestoreEntitySequence", sequenceName = "SEQ_ACCESS_RESTORE_ACTION_ENTITY", allocationSize = 1000)
    private Long id;

    @Column(name = "SESSION_ID", nullable = false)
    private String sessionId;

    @Column(name = "LOGIN", nullable = false)
    private String login;

    @Column(name = "STATE", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionState state = ActionState.NEW;

    @Embedded
    private OtpActionEntity otpAction;
}
