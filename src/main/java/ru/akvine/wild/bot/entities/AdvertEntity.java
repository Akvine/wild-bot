package ru.akvine.wild.bot.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.entities.base.SoftBaseEntity;
import ru.akvine.wild.bot.enums.AdvertStatus;
import ru.akvine.wild.bot.enums.AdvertType;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
@Table(name = "ADVERT_ENTITY")
@Entity
public class AdvertEntity extends SoftBaseEntity {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "advertEntitySeq")
    @SequenceGenerator(name = "advertEntitySeq", sequenceName = "SEQ_ADVERT_ENTITY", allocationSize = 1000)
    private Long id;

    @Column(name = "UUID", nullable = false)
    private String uuid;

    @Column(name = "EXTERNAL_ID", nullable = false)
    private int externalId;

    @Column(name = "EXTERNAL_TITLE", nullable = false)
    private String externalTitle;

    @Column(name = "CHANGE_TIME", nullable = false)
    private Date changeTime;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private AdvertStatus status;

    @Column(name = "ORDINAL_STATUS", nullable = false)
    private int ordinalStatus;

    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private AdvertType type;

    @Column(name = "ORDINAL_TYPE", nullable = false)
    private int ordinalType;

    @Column(name = "CPM", nullable = false)
    private int cpm;

    @Column(name = "START_CHECK_DATE_TIME")
    private LocalDateTime startCheckDateTime;

    @Column(name = "NEXT_CHECK_DATE_TIME")
    private LocalDateTime nextCheckDateTime;

    @Column(name = "START_BUDGET_SUM")
    private Integer startBudgetSum;

    @Column(name = "CHECK_BUDGET_SUM")
    private Integer checkBudgetSum;

    @Column(name = "AVAILABLE_FOR_START")
    private LocalDateTime availableForStart = LocalDateTime.now();

    @OneToOne
    @JoinColumn(name = "LAUNCHED_BY_CLIENT_ID")
    private ClientEntity client;

    @Column(name = "IS_LOCKED", nullable = false)
    private boolean locked;

    @OneToOne
    @JoinColumn(name = "CARD_ID", nullable = false)
    private CardEntity card;

    @Transient
    public boolean isAvailableForStart() {
        return LocalDateTime.now().isAfter(availableForStart);
    }
}
