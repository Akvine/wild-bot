package ru.akvine.marketspace.bot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.marketspace.bot.entities.BlockedCredentialsEntity;

import java.util.Optional;

public interface BlockedCredentialsRepository extends JpaRepository<BlockedCredentialsEntity, Long> {
    @Query("from BlockedCredentialsEntity bce where bce.uuid = :login")
    Optional<BlockedCredentialsEntity> findByLogin(@Param("login") String login);
}
