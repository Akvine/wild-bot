package ru.akvine.wild.bot.repositories.security;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.wild.bot.entities.security.RegistrationActionEntity;

public interface RegistrationActionRepository extends ActionRepository<RegistrationActionEntity> {
    @Query("from RegistrationActionEntity rae where rae.login = :login")
    RegistrationActionEntity findCurrentAction(@Param("login") String login);
}
