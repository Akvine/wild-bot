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
            "ae.deletedDate is null and ae.deleted = false")
    List<AdvertEntity> findByStatuses(@Param("statuses") List<AdvertStatus> states);

    @Query("from AdvertEntity ae where ae.status in :statuses " +
            "and " +
            "ae.categoryId = :categoryId " +
            "and " +
            "ae.deletedDate is null and ae.deleted = false")
    List<AdvertEntity> findByStatusesAndCategoryId(@Param("statuses") List<AdvertStatus> states,
                                                   @Param("categoryId") String categoryId);

    @Query("from AdvertEntity ae where ae.uuid = :uuid and ae.deletedDate is null and ae.deleted = false")
    Optional<AdvertEntity> findByUuid(@Param("uuid") String uuid);

    @Query("from AdvertEntity  ae where ae.advertId = :advertId and ae.deletedDate is null and ae.deleted = false")
    Optional<AdvertEntity> findByAdvertId(@Param("advertId") String advertId);
}
