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
@Table(name = "CARD_ENTITY")
public class CardEntity extends SoftBaseEntity {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cardEntitySeq")
    @SequenceGenerator(name = "cardEntitySeq", sequenceName = "SEQ_CARD_ENTITY", allocationSize = 1000)
    private Long id;

    @Column(name = "UUID", nullable = false)
    private String uuid;

    @Column(name = "ITEM_ID", nullable = false)
    private int itemId;

    @Column(name = "ITEM_TITLE", nullable = false)
    private String itemTitle;

    @Column(name = "CATEGORY_ID", nullable = false)
    private int categoryId;

    @Column(name = "CATEGORY_TITLE", nullable = false)
    private String categoryTitle;

    @Column(name = "BARCODE", nullable = false)
    private String barcode;
}
