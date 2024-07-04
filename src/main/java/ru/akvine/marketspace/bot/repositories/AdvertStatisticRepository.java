package ru.akvine.marketspace.bot.repositories;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.akvine.marketspace.bot.entities.AdvertStatisticEntity;

import java.util.List;

public interface AdvertStatisticRepository extends JpaRepository<AdvertStatisticEntity, Long> {
    @Query("from AdvertStatisticEntity ase join ase.advertEntity ae where " +
            "ase.deleted = false and ae.deleted = false")
    @NotNull
    List<AdvertStatisticEntity> findAll();
}
