package ru.akvine.marketspace.bot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.marketspace.bot.entities.ClientSubscriptionEntity;

import java.util.Optional;

public interface ClientSubscriptionRepository extends JpaRepository<ClientSubscriptionEntity, Long> {
    @Query("from ClientSubscriptionEntity cse join cse.client csec " +
            "where cses.chatId = :chatId")
    Optional<ClientSubscriptionEntity> findByChatId(@Param("chatId") String chatId);
}
