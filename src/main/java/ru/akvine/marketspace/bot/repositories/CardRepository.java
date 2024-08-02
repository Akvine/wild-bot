package ru.akvine.marketspace.bot.repositories;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.marketspace.bot.entities.CardEntity;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<CardEntity, Long> {
    @Query("from CardEntity ce where ce.deleted = false and ce.deletedDate is null")
    @NotNull
    List<CardEntity> findAll();

    @Query("from CardEntity ce where ce.itemId = :itemId and ce.deleted = false and ce.deletedDate is null")
    Optional<CardEntity> findByItemId(@Param("itemId") int itemId);

    @Query("from CardEntity ce where ce.categoryId = :categoryId and ce.deleted = false")
    List<CardEntity> findByCategoryId(@Param("categoryId") int categoryId);
}
