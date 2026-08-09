package ru.akvine.wild.bot.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.wild.bot.entities.ClientBlockedCredentialsEntity;

public interface ClientBlockedCredentialsRepository extends JpaRepository<ClientBlockedCredentialsEntity, Long> {
    @Query("from ClientBlockedCredentialsEntity cbce where cbce.chatId = :chatId")
    Optional<ClientBlockedCredentialsEntity> findByChatId(@Param("chatId") String uuid);
}
