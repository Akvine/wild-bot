package ru.akvine.wild.bot.repositories.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.wild.bot.entities.infrastructure.ClientStatesEntity;

public interface ClientStatesRepository extends JpaRepository<ClientStatesEntity, Long> {
    @Query("from ClientStatesEntity cse where cse.identifier = :identifier")
    Optional<ClientStatesEntity> findByIdentifier(@Param("identifier") String identifier);
}
