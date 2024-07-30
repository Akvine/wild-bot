package ru.akvine.marketspace.bot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.marketspace.bot.entities.IterationCounterEntity;

import java.util.Optional;

public interface IterationCounterRepository extends JpaRepository<IterationCounterEntity, Long> {
    @Query("from IterationCounterEntity ice where ice.advertId = :advertId")
    Optional<IterationCounterEntity> findByAdvertId(@Param("advertId") int advertId);
}
