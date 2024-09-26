package ru.akvine.wild.bot.entities.security;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Entity
@Table(name = "OTP_COUNTER_ENTITY")
@Getter
@Setter
@Accessors(chain = true)
public class OtpCounterEntity {
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "otpCounterEntitySequence")
    @SequenceGenerator(name = "otpCounterEntitySequence", sequenceName = "SEQ_OTP_COUNTER_ENTITY", allocationSize = 1000)
    private Long id;

    @Column(name = "LOGIN", nullable = false)
    private String login;

    @Column(name = "OTP_VALUE", nullable = false)
    private long value = 1L;

    @Column(name = "LAST_UPDATED", nullable = false)
    private LocalDateTime lastUpdated;
}
