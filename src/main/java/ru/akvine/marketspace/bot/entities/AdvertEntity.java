package ru.akvine.marketspace.bot.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.entities.base.SoftBaseEntity;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.enums.AdvertType;

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

    @Column(name = "ADVERT_ID", nullable = false)
    private String advertId;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "CHANGE_TIME", nullable = false)
    private Date changeTime;

    @Column(name = "CATEGORY_ID")
    private String categoryId;

    @Column(name = "ITEM_ID", nullable = false)
    private String itemId;

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

    @Column(name = "CPM")
    private Integer cpm;

    @Column(name = "START_CHECK_DATE_TIME")
    private LocalDateTime startCheckDateTime;

    @Column(name = "NEXT_CHECK_DATE_TIME")
    private LocalDateTime nextCheckDateTime;

    @Column(name = "START_BUDGET_SUM")
    private Integer startBudgetSum;

    @Column(name = "CHECK_BUDGET_SUM")
    private Integer checkBudgetSum;

    @OneToOne
    @JoinColumn(name = "LAUNCHED_BY_CLIENT_ID")
    private ClientEntity client;
}
