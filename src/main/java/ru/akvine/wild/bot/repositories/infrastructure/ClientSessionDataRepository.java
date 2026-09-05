package ru.akvine.wild.bot.repositories.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.wild.bot.entities.infrastructure.ClientSessionDataEntity;
import ru.akvine.wild.bot.enums.BotType;

public interface ClientSessionDataRepository extends JpaRepository<ClientSessionDataEntity, Long> {
    @Query("from ClientSessionDataEntity csde where csde.chatId = :chatId and csde.botType = :botType")
    Optional<ClientSessionDataEntity> findByChatIdAndBotType(
            @Param("chatId") String chatId, @Param("botType") BotType botType);
}
