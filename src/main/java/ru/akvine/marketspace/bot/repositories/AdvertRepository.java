package ru.akvine.marketspace.bot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.marketspace.bot.entities.AdvertEntity;
import ru.akvine.marketspace.bot.enums.AdvertStatus;

import java.util.List;
import java.util.Optional;

public interface AdvertRepository extends JpaRepository<AdvertEntity, Long> {
    @Query("from AdvertEntity ae where ae.status in :statuses " +
            "and " +
            "ae.deleted = false")
    List<AdvertEntity> findByStatuses(@Param("statuses") List<AdvertStatus> states);

    @Query("from AdvertEntity ae where ae.status in :statuses " +
            "and " +
            "ae.categoryId = :categoryId " +
            "and " +
            "ae.deleted = false")
    List<AdvertEntity> findByStatusesAndCategoryId(@Param("statuses") List<AdvertStatus> states,
                                                   @Param("categoryId") Integer categoryId);

    @Query("from AdvertEntity ae join ae.client c where ae.status in :statuses " +
            "and " +
            "c.id = :id " +
            "and " +
            "ae.deleted = false")
    List<AdvertEntity> findByClientIdAndStatuses(@Param("id") Long clientId, List<AdvertStatus> statuses);

    @Query("from AdvertEntity ae where ae.uuid = :uuid and ae.deleted = false")
    Optional<AdvertEntity> findByUuid(@Param("uuid") String uuid);

    @Query("from AdvertEntity ae where ae.advertId = :advertId and ae.deleted = false")
    Optional<AdvertEntity> findByAdvertId(@Param("advertId") int advertId);

    @Query("from AdvertEntity ae join ae.client c " +
            "where ae.advertId = :advertId and " +
            "c.id = :clientId and " +
            "ae.deleted = false")
    Optional<AdvertEntity> findByAdvertIdAndClientId(@Param("advertId") int advertId,
                                                     @Param("clientId") long clientId);
}
