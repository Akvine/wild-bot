package ru.akvine.marketspace.bot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.marketspace.bot.entities.SubscriptionEntity;

import java.util.Optional;

public interface ClientSubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {
    @Query("from SubscriptionEntity se join se.client sec " +
            "where sec.chatId = :chatId")
    Optional<SubscriptionEntity> findByChatId(@Param("chatId") String chatId);
}
