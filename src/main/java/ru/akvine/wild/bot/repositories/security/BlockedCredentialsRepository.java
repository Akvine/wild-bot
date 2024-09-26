package ru.akvine.wild.bot.repositories.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.wild.bot.entities.security.BlockedCredentialsEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BlockedCredentialsRepository extends JpaRepository<BlockedCredentialsEntity, Long> {
    @Query("from BlockedCredentialsEntity bce where bce.login = :login")
    Optional<BlockedCredentialsEntity> findByLogin(@Param("login") String login);

    @Query("from BlockedCredentialsEntity bce where bce.blockEndDate < :now")
    List<BlockedCredentialsEntity> findExpired(@Param("now")LocalDateTime now);
}
