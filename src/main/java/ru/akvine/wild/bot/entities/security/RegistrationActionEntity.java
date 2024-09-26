package ru.akvine.wild.bot.entities.security;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.enums.security.ActionState;

@Entity
@Table(name = "REGISTRATION_ACTION_ENTITY")
@Getter
@Setter
@Accessors(chain = true)
public class RegistrationActionEntity implements OneTimePasswordable {
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "registrationActionEntitySequence")
    @SequenceGenerator(name = "registrationActionEntitySequence", sequenceName = "SEQ_REGISTRATION_ACTION_ENTITY", allocationSize = 1000)
    private Long id;

    @Column(name = "LOGIN", nullable = false)
    private String login;

    @Column(name = "SESSION_ID", nullable = false)
    private String sessionId;

    @Column(name = "STATE", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionState state = ActionState.NEW;

    @Embedded
    private OtpActionEntity otpAction;
}
