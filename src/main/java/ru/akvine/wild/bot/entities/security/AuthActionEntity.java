package ru.akvine.wild.bot.entities.security;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.exceptions.security.NoMoreOtpInvalidAttemptsException;

@Entity
@Table(name = "AUTH_ACTION_ENTITY")
@Data
@Accessors(chain = true)
public class AuthActionEntity implements OneTimePasswordable, AccountPasswordable {
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "authActionEntitySequence")
    @SequenceGenerator(name = "authActionEntitySequence", sequenceName = "SEQ_AUTH_ACTION_ENTITY", allocationSize = 1000)
    private Long id;

    @Column(name = "SESSION_ID", nullable = false)
    private String sessionId;

    @Column(name = "LOGIN", nullable = false)
    private String login;

    @Column(name = "PWD_INVALID_ATTEMPTS_LEFT", nullable = false)
    private int pwdInvalidAttemptsLeft;

    @Embedded
    private OtpActionEntity otpAction;

    @Transient
    public int decrementPwdInvalidAttemptsLeft() {
        if (this.pwdInvalidAttemptsLeft <= 0) {
            throw new NoMoreOtpInvalidAttemptsException("No more attempts to authenticate");
        }
        return --this.pwdInvalidAttemptsLeft;
    }
}
