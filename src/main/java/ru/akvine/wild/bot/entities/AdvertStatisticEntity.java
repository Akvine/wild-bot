package ru.akvine.marketspace.bot.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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

    @Column(name = "VIEWS")
    private String views;

    @Column(name = "CLICKS")
    private String clicks;

    @Column(name = "CTR")
    private String ctr;

    @Column(name = "CPC")
    private String cpc;

    @Column(name = "SUM")
    private String sum;

    @Column(name = "ATBS")
    private String atbs;

    @Column(name = "ORDERS")
    private String orders;

    @Column(name = "CR")
    private String cr;

    @Column(name = "SHKS")
    private String shks;

    @Column(name = "SUM_PRICE")
    private String sumPrice;

    @Column(name = "PHOTO")
    @ToString.Exclude
    private byte[] photo;

    @Column(name = "IS_ACTIVE")
    private boolean active;

    @ManyToOne
    @JoinColumn(name = "ADVERT_ID", nullable = false)
    private AdvertEntity advertEntity;

    @ManyToOne
    @JoinColumn(name = "CLIENT_ID", nullable = false)
    private ClientEntity client;
}
