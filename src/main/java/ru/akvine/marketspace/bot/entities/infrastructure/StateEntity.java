package ru.akvine.marketspace.bot.entities.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.entities.base.BaseEntity;
import ru.akvine.marketspace.bot.enums.ClientState;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "STATE_ENTITY")
public class StateEntity extends BaseEntity {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stateEntitySeq")
    @SequenceGenerator(name = "stateEntitySeq", sequenceName = "SEQ_STATE_ENTITY", allocationSize = 1000)
    private Long id;

    @Column(name = "CHAT_ID", nullable = false)
    private String chatId;

    @Column(name = "STATE")
    @Enumerated(EnumType.STRING)
    private ClientState clientState;
}
