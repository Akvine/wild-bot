package ru.akvine.wild.bot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.wild.bot.entities.AdvertStatisticEntity;

import java.util.List;
import java.util.Optional;

public interface AdvertStatisticRepository extends JpaRepository<AdvertStatisticEntity, Long> {
    @Query("from AdvertStatisticEntity ase join ase.advertEntity ae join ase.client c where " +
            "c.id = :id and " +
            "ase.active = false and " +
            "ase.deleted = false")
    List<AdvertStatisticEntity> findByClientId(@Param("id") Long clientId);

    @Query("from AdvertStatisticEntity ase join ase.advertEntity ae join ase.client c where " +
            "c.id = :clientId and " +
            "ae.id = :advertId and " +
            "ase.active = true and " +
            "c.deleted = false and ase.deleted = false and ae.deleted = false")
    Optional<AdvertStatisticEntity> findByClientIdAndAdvertId(@Param("clientId") Long clientId,
                                                              @Param("advertId") Long advertId);

    @Query("from AdvertStatisticEntity ase join ase.client c where " +
            "c.id = :clientId and " +
            "ase.id = :id and " +
            "c.deleted = false and ase.deleted = false")
    Optional<AdvertStatisticEntity> findByClientIdAndId(@Param("clientId") Long clientId,
                                                              @Param("id") Long id);
}
