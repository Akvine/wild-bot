package ru.akvine.wild.bot.entities.infrastructure;

import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.converters.ClientStatesConverter;
import ru.akvine.wild.bot.entities.base.BaseEntity;
import ru.akvine.wild.bot.enums.ClientState;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Table(name = "CLIENT_STATES_ENTITY")
@Entity
public class ClientStatesEntity extends BaseEntity {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clientStatesEntitySeq")
    @SequenceGenerator(name = "clientStatesEntitySeq", sequenceName = "SEQ_CLIENT_STATES_ENTITY", allocationSize = 1000)
    private Long id;

    @Column(name = "IDENTIFIER", nullable = false, updatable = false)
    private String identifier;

    @Column(name = "STATES", nullable = false)
    @Convert(converter = ClientStatesConverter.class)
    private List<ClientState> states;
}
