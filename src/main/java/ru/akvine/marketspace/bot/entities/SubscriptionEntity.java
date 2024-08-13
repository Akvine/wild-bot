package ru.akvine.marketspace.bot.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.entities.base.BaseEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "SUBSCRIPTION_ENTITY")
@Entity
public class SubscriptionEntity extends BaseEntity {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subscriptionEntitySeq")
    @SequenceGenerator(name = "subscriptionEntitySeq", sequenceName = "SEQ_SUBSCRIPTION_ENTITY", allocationSize = 1000)
    private Long id;

    @OneToOne
    @JoinColumn(name = "CLIENT_ID", nullable = false)
    private ClientEntity client;

    @Column(name = "EXPIRES_AT", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "IS_NOTIFIED_THAT_EXPIRES", nullable = false)
    private boolean notifiedThatExpires;

    @Transient
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
