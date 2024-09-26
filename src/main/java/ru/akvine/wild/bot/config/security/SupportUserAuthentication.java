package ru.akvine.wild.bot.config.security;


import com.google.common.base.Preconditions;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

public class SupportUserAuthentication implements Authentication {
    @Getter
    private final long id;
    @Getter
    private final String uuid;
    private final String email;

    public SupportUserAuthentication(long id,
                                     String uuid,
                                     String email) {
        Preconditions.checkNotNull(uuid, "uuid is null");
        Preconditions.checkNotNull(email, "email is null");

        this.id = id;
        this.uuid = uuid;
        this.email = email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public Object getCredentials() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return email;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return email;
    }
}
