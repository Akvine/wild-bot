package ru.akvine.wild.bot.entities.infrastructure;

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
@Table(name = "ITERATION_COUNTER_ENTITY")
@Entity
public class IterationCounterEntity extends BaseEntity {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "iterationCounterEntitySeq")
    @SequenceGenerator(name = "iterationCounterEntitySeq", sequenceName = "SEQ_ITERATION_COUNTER_ENTITY", allocationSize = 1000)
    private Long id;

    @Column(name = "ADVERT_ID", nullable = false, unique = true)
    private int advertId;

    @Column(name = "COUNT", nullable = false)
    private int count;

    @Transient
    public void increase() {
        count += 1;
    }
}
