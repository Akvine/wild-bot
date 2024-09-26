package ru.akvine.wild.bot.repositories.security;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.wild.bot.entities.security.AccessRestoreActionEntity;

public interface AccessRestoreActionRepository extends ActionRepository<AccessRestoreActionEntity> {
    @Query("from AccessRestoreActionEntity arae where arae.login = :login")
    AccessRestoreActionEntity findCurrentAction(@Param("login") String login);
}
