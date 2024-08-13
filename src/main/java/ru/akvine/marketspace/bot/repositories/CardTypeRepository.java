package ru.akvine.marketspace.bot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.akvine.marketspace.bot.entities.CardTypeEntity;

public interface CardTypeRepository extends JpaRepository<CardTypeEntity, Long> {
}
