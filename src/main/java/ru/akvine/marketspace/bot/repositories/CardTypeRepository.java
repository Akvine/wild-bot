package ru.akvine.marketspace.bot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.marketspace.bot.entities.CardTypeEntity;

import java.util.Optional;

public interface CardTypeRepository extends JpaRepository<CardTypeEntity, Long> {
    @Query("from CardTypeEntity cte where cte.type = :type")
    Optional<CardTypeEntity> findByType(@Param("type") String type);
}
