package ru.akvine.wild.bot.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.entities.base.BaseEntity;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "CARD_TYPE_ENTITY")
public class CardTypeEntity extends BaseEntity {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cardTypeEntitySeq")
    @SequenceGenerator(name = "cardTypeEntitySeq", sequenceName = "SEQ_CARD_TYPE_ENTITY", allocationSize = 1000)
    private Long id;

    @Column(name = "TYPE", nullable = false)
    private String type;
}
