package ru.akvine.marketspace.bot.repositories;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.akvine.marketspace.bot.entities.ClientEntity;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<ClientEntity, Long> {
    @Query("from ClientEntity ce where ce.chatId = :chatId and ce.deleted = false and ce.deletedDate is null")
    Optional<ClientEntity> findByChatId(@Param("chatId") String chatId);

    @Query("from ClientEntity ce where ce.uuid = :uuid and ce.deleted = false and ce.deletedDate is null")
    Optional<ClientEntity> findByUuid(@Param("uuid") String uuid);

    @Query("from ClientEntity ce where ce.username = :username and ce.deleted = false and ce.deletedDate is null")
    Optional<ClientEntity> findByUsername(@Param("username") String username);

    @Query("from ClientEntity ce where ce.username in :usernames and ce.deleted = false")
    List<ClientEntity> findByUsernames(@Param("usernames") List<String> usernames);

    @Query("from ClientEntity ce where ce.chatId in :chatIds and ce.deleted = false and ce.deletedDate is null")
    List<ClientEntity> findByListChatId(@Param("chatIds") List<String> chatIds);

    @Query("from ClientEntity ce where ce.deleted = false and ce.deletedDate is null")
    @NotNull
    List<ClientEntity> findAll();
}
