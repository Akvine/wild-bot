package ru.akvine.wild.bot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.wild.bot.entities.AdvertEntity;
import ru.akvine.wild.bot.enums.AdvertStatus;

import java.util.List;
import java.util.Optional;

public interface AdvertRepository extends JpaRepository<AdvertEntity, Long> {
    @Query("from AdvertEntity ae where ae.status in :statuses " +
            "and " +
            "ae.deleted = false")
    List<AdvertEntity> findByStatuses(@Param("statuses") List<AdvertStatus> states);

    @Query("from AdvertEntity ae join ae.card aec join aec.cardType aecct where ae.status in :statuses " +
            "and " +
            "aecct.type = :cardType and " +
            "aec.categoryId = :categoryId " +
            "and " +
            "ae.deleted = false")
    List<AdvertEntity> findByStatusesAndCardTypeAndCategoryId(@Param("statuses") List<AdvertStatus> states,
                                                              @Param("cardType") String cardType,
                                                              @Param("categoryId") Integer categoryId);

    @Query("from AdvertEntity ae join ae.client c where ae.status in :statuses " +
            "and " +
            "c.id = :id " +
            "and " +
            "ae.deleted = false")
    List<AdvertEntity> findByClientIdAndStatuses(@Param("id") Long clientId, List<AdvertStatus> statuses);

    @Query("from AdvertEntity ae where ae.uuid = :uuid and ae.deleted = false")
    Optional<AdvertEntity> findByUuid(@Param("uuid") String uuid);

    @Query("from AdvertEntity ae where ae.externalId = :externalId and ae.deleted = false")
    Optional<AdvertEntity> findByExternalId(@Param("externalId") int externalId);

    @Query("from AdvertEntity ae join ae.client c " +
            "where ae.externalId = :externalId and " +
            "c.id = :clientId and " +
            "ae.deleted = false")
    Optional<AdvertEntity> findByExternalIdAndClientId(@Param("externalId") int externalId,
                                                       @Param("clientId") long clientId);
}
