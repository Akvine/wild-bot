package ru.akvine.marketspace.bot.repositories.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.marketspace.bot.entities.infrastructure.StateEntity;

import java.util.Optional;

public interface StateRepository extends JpaRepository<StateEntity, Long> {
    @Query("from StateEntity se where se.chatId = :chatId")
    Optional<StateEntity> findByChatId(@Param("chatId") String chatId);
}
