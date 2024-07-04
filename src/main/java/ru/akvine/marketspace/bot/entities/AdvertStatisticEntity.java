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
@Table(name = "ADVERT_STATISTIC_ENTITY")
public class AdvertStatisticEntity extends SoftBaseEntity {
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "advertStatisticEntitySequence")
    @SequenceGenerator(name = "advertStatisticEntitySequence", sequenceName = "SEQ_ADVERT_STATISTIC_ENTITY", allocationSize = 1000)
    private Long id;

    @Column(name = "VIEWS", nullable = false)
    private String views;

    @Column(name = "CLICKS", nullable = false)
    private String clicks;

    @Column(name = "CTR", nullable = false)
    private String ctr;

    @Column(name = "CPC", nullable = false)
    private String cpc;

    @Column(name = "SUM", nullable = false)
    private String sum;

    @Column(name = "ATBS", nullable = false)
    private String atbs;

    @Column(name = "ORDERS", nullable = false)
    private String orders;

    @Column(name = "CR", nullable = false)
    private String cr;

    @Column(name = "SHKS", nullable = false)
    private String shks;

    @Column(name = "SUM_PRICE", nullable = false)
    private String sumPrice;

    @ManyToOne
    @JoinColumn(name = "ADVERT_ID")
    private AdvertEntity advertEntity;
}
