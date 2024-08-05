package ru.akvine.marketspace.bot.repositories.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.marketspace.bot.entities.infrastructure.ClientSessionDataEntity;

import java.util.Optional;

public interface ClientSessionDataRepository extends JpaRepository<ClientSessionDataEntity, Long> {
    @Query("from ClientSessionDataEntity csde where csde.chatId = :chatId")
    Optional<ClientSessionDataEntity> findByChatId(@Param("chatId") String chatId);
}
