package ru.akvine.marketspace.bot.services;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.entities.ClientEntity;
import ru.akvine.marketspace.bot.exceptions.BlockedCredentialsException;
import ru.akvine.marketspace.bot.exceptions.ClientNotFoundException;
import ru.akvine.marketspace.bot.repositories.ClientRepository;
import ru.akvine.marketspace.bot.services.domain.ClientBean;
import ru.akvine.marketspace.bot.services.dto.ClientCreate;
import ru.akvine.marketspace.bot.utils.UUIDGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {
    private final ClientRepository clientRepository;
    private final BlockingService blockingService;

    @Nullable
    public ClientBean findByChatId(String chatId) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        logger.debug("Find client by chatId = {}", chatId);

        Optional<ClientEntity> client = clientRepository.findByChatId(chatId);
        return client.map(ClientBean::new).orElse(null);
    }

    public ClientBean create(ClientCreate clientCreate) {
        Preconditions.checkNotNull(clientCreate, "ClientCreate is null");
        logger.info("Create client = [{}]", clientCreate);

        ClientEntity clientEntity = new ClientEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setChatId(clientCreate.getChatId())
                .setUsername(clientCreate.getUsername())
                .setFirstName(clientCreate.getFirstName())
                .setLastName(clientCreate.getLastName());

        return new ClientBean(clientRepository.save(clientEntity));
    }

    public void checkIsBlocked(String chatId) {
        logger.debug("Check client is blocked by chat id = {}", chatId);
        String uuid = verifyExistsByChatId(chatId).getUuid();
        LocalDateTime blockDateTime = blockingService.getEndBlockDate(uuid);
        if (blockDateTime != null) {
            String errorMessage = String.format("Client with chat id = [%s] has blocked until = [%s]!", chatId, blockDateTime);
            throw new BlockedCredentialsException(errorMessage, blockDateTime.toLocalDate());
        }
    }

    public ClientEntity verifyExistsByClientUuid(String clientUuid) {
        Preconditions.checkNotNull(clientUuid, "clientUuid is null");
        logger.info("Verify client exists by uuid = {}", clientUuid);
        return clientRepository.findByUuid(clientUuid).orElseThrow(() -> new ClientNotFoundException("Client has no with uuid = [" + clientUuid + "]!"));
    }

    public ClientBean getByChatId(String chatId) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        logger.debug("Get client by uuid = {}", chatId);
        return new ClientBean(verifyExistsByChatId(chatId));
    }

    public ClientEntity verifyExistsByChatId(String chatId) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        logger.debug("Verify client exists by chat with id = {}", chatId);
        return clientRepository.findByChatId(chatId).orElseThrow(() -> new ClientNotFoundException("Client has no with chatId = [" + chatId + "]!"));
    }

    public ClientEntity verifyExistsByUsername(String username) {
        Preconditions.checkNotNull(username, "username is null");
        logger.info("Verify client exists by username = {}", username);
        return clientRepository.findByUsername(username).orElseThrow(() -> new ClientNotFoundException("Client has no with username = [" + username + "]!"));
    }

    public List<ClientBean> getByListChatId(List<String> chatIds) {
        Preconditions.checkNotNull(chatIds, "chatIds is null");
        logger.info("Get by chat ids = {}", chatIds);
        return clientRepository
                .findByListChatId(chatIds)
                .stream()
                .map(ClientBean::new)
                .toList();
    }

    public List<ClientBean> getAll() {
        logger.info("Get all clients");
        return clientRepository
                .findAll()
                .stream()
                .map(ClientBean::new)
                .toList();
    }

    public List<ClientBean> getAllByUsernames(List<String> usernames) {
        logger.info("Get all clients by usernames = {}", usernames);
        return clientRepository
                .findByUsernames(usernames)
                .stream()
                .map(ClientBean::new)
                .toList();
    }
}
