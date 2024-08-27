package ru.akvine.marketspace.bot.repositories;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.marketspace.bot.entities.CardEntity;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<CardEntity, Long>, JpaSpecificationExecutor<CardEntity> {
    @Query("from CardEntity ce where ce.deleted = false")
    @NotNull
    List<CardEntity> findAll();

    @Query("from CardEntity ce where ce.externalId = :externalId and ce.deleted = false")
    Optional<CardEntity> findByExternalId(@Param("externalId") int externalId);

    @Query("from CardEntity ce where ce.categoryId = :categoryId and ce.deleted = false")
    List<CardEntity> findByCategoryId(@Param("categoryId") int categoryId);

    @Query("from CardEntity ce join ce.cardType cte where cte.type = :type and ce.deleted = false")
    List<CardEntity> findByCardType(@Param("type") String cardType);
}
