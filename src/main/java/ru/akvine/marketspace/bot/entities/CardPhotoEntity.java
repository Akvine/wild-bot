package ru.akvine.marketspace.bot.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.entities.base.BaseEntity;

@Getter
@Setter
@Accessors(chain = true)
@Table(name = "CARD_PHOTO_ENTITY")
@Entity
public class CardPhotoEntity extends BaseEntity {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cardPhotoEntitySeq")
    @SequenceGenerator(name = "cardPhotoEntitySeq", sequenceName = "SEQ_CARD_PHOTO_ENTITY", allocationSize = 1000)
    private Long id;

    @Column(name = "BIG_URL")
    private String bigUrl;

    @ManyToOne
    @JoinColumn(name = "CARD_ID", nullable = false)
    private CardEntity cardEntity;
}
