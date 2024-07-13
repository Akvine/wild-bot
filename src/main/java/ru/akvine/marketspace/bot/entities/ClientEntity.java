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
@Table(name = "CLIENT_ENTITY")
public class ClientEntity extends SoftBaseEntity {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clientEntitySeq")
    @SequenceGenerator(name = "clientEntitySeq", sequenceName = "SEQ_CLIENT_ENTITY", allocationSize = 1000)
    private Long id;

    @Column(name = "UUID", nullable = false)
    private String uuid;

    @Column(name = "CHAT_ID", nullable = false)
    private String chatId;

    @Column(name = "USERNAME", nullable = false)
    private String username;

    @Column(name = "FIRS_TNAME", nullable = false)
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "IS_IN_WHITELIST", nullable = false)
    private boolean inWhiteList;
}
