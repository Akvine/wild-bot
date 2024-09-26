package ru.akvine.wild.bot.repositories.security;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.wild.bot.entities.security.AuthActionEntity;

public interface AuthActionRepository extends ActionRepository<AuthActionEntity> {
    @Query("from AuthActionEntity aae where aae.login = :login")
    AuthActionEntity findCurrentAction(@Param("login") String login);
}
