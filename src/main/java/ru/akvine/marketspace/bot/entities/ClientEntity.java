package ru.akvine.marketspace.bot.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.entities.base.SoftBaseEntity;

@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(name = "CLIENT_ENTITY")
public class ClientEntity extends SoftBaseEntity {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clientEntitySeq")
    @SequenceGenerator(name = "clientEntitySeq", sequenceName = "SEQ_CLIENT_ENTITY", allocationSize = 1000)
    private Long id;

    @Column(name = "UUID", nullable = false)
    private String uuid;

    @Column(name = "CHAT_ID", nullable = false)
    private String chatId;

    @Column(name = "USERNAME", nullable = false)
    private String username;

    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "AVAILABLE_TESTS_COUNT")
    private int availableTestsCount;

    @Transient
    public void increaseAvailableTestsCount(int addCount) {
        availableTestsCount += addCount;
    }

    @Transient
    public void decreaseOneTest() {
        decreaseAvailableTestsCount(1);
    }

    @Transient
    public void decreaseAvailableTestsCount(int deleteCount) {
        availableTestsCount -= deleteCount;
    }
}
