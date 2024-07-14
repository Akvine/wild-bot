package ru.akvine.marketspace.bot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.marketspace.bot.entities.CardPhotoEntity;

import java.util.List;

public interface CardPhotoRepository extends JpaRepository<CardPhotoEntity, Long> {
    @Query("from CardPhotoEntity cpe join cpe.cardEntity cpece where cpece.id = :id")
    List<CardPhotoEntity> findByCardId(@Param("id") Long cardId);
}
