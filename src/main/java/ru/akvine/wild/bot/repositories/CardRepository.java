package ru.akvine.wild.bot.repositories;

import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.wild.bot.entities.CardEntity;
import ru.akvine.wild.bot.enums.BotType;

public interface CardRepository extends JpaRepository<CardEntity, Long>, JpaSpecificationExecutor<CardEntity> {
    @Query("from CardEntity ce where ce.deleted = false and ce.ownerClient.uuid = :clientUuid")
    @NotNull
    List<CardEntity> findAll(@Param("clientUuid") String clientUuid);

    @Query("from CardEntity ce where ce.externalId = :externalId and ce.deleted = false")
    Optional<CardEntity> findByExternalId(@Param("externalId") int externalId);

    @Query("from CardEntity ce join fetch ce.ownerClient join fetch ce.cardType "
            + "where ce.categoryId = :categoryId and ce.deleted = false")
    List<CardEntity> findByCategoryId(@Param("categoryId") int categoryId);

    @Query("from CardEntity ce join fetch ce.cardType cte join fetch ce.ownerClient "
            + "where cte.type = :type and ce.deleted = false")
    List<CardEntity> findByCardType(@Param("type") String cardType);

    @Query("from CardEntity ce join fetch ce.ownerClient cec join fetch ce.cardType "
            + "where cec.chatId = :chatId and cec.botType = :botType and "
            + "cec.deleted = false "
            + "and "
            + "ce.deleted = false")
    List<CardEntity> findByChatIdAndBotType(@Param("chatId") String chatId, @Param("botType") BotType botType);

    @Override
    @EntityGraph(attributePaths = {"ownerClient", "cardType"})
    Page<CardEntity> findAll(Specification<CardEntity> spec, Pageable pageable);
}
