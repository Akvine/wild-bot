package ru.akvine.wild.bot.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.entities.base.SoftBaseEntity;

@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(name = "CARD_ENTITY")
public class CardEntity extends SoftBaseEntity {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cardEntitySeq")
    @SequenceGenerator(name = "cardEntitySeq", sequenceName = "SEQ_CARD_ENTITY", allocationSize = 1000)
    private Long id;

    @Column(name = "UUID", nullable = false)
    private String uuid;

    @Column(name = "EXTERNAL_ID", nullable = false)
    private int externalId;

    @Column(name = "EXTERNAL_TITLE", nullable = false)
    private String externalTitle;

    @Column(name = "CATEGORY_ID", nullable = false)
    private int categoryId;

    @Column(name = "CATEGORY_TITLE", nullable = false)
    private String categoryTitle;

    @Column(name = "BARCODE", nullable = false)
    private String barcode;

    @ManyToOne
    @JoinColumn(name = "CARD_TYPE_ID", nullable = false)
    private CardTypeEntity cardType;
}
