package ru.akvine.marketspace.bot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.marketspace.bot.entities.AdvertStatisticEntity;

import java.util.List;

public interface AdvertStatisticRepository extends JpaRepository<AdvertStatisticEntity, Long> {
    @Query("from AdvertStatisticEntity ase join ase.advertEntity ae join ase.client c where " +
            "c.id = :id and " +
            "ase.deleted = false")
    List<AdvertStatisticEntity> findByClientId(@Param("id") Long clientId);
}
