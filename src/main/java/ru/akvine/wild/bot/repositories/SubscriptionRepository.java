package ru.akvine.wild.bot.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.wild.bot.entities.SubscriptionEntity;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {
    @Query("from SubscriptionEntity se join se.client sec " + "where sec.chatId = :chatId")
    Optional<SubscriptionEntity> findByChatId(@Param("chatId") String chatId);
}
