package ru.akvine.wild.bot.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.wild.bot.entities.ClientBlockedCredentialsEntity;
import ru.akvine.wild.bot.enums.BotType;

public interface ClientBlockedCredentialsRepository extends JpaRepository<ClientBlockedCredentialsEntity, Long> {
    @Query("from ClientBlockedCredentialsEntity cbce where cbce.chatId = :chatId and cbce.botType = :botType")
    Optional<ClientBlockedCredentialsEntity> findByChatIdAndBotType(
            @Param("chatId") String chatId, @Param("botType") BotType botType);
}
