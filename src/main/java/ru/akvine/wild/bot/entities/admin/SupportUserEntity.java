package ru.akvine.wild.bot.entities.admin;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.akvine.wild.bot.entities.base.BaseEntity;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Table(name = "SUPPORT_USER_ENTITY")
@Entity
public class SupportUserEntity extends BaseEntity implements UserDetails {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "supportUserEntitySeq")
    @SequenceGenerator(name = "supportUserEntitySeq", sequenceName = "SEQ_SUPPORT_USER_ENTITY", allocationSize = 1000)
    private Long id;

    @Column(name = "UUID", nullable = false)
    private String uuid;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "HASH", nullable = false)
    private String hash;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return hash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
