package ru.akvine.marketspace.bot.services;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.entities.ClientEntity;
import ru.akvine.marketspace.bot.exceptions.BlockedCredentialsException;
import ru.akvine.marketspace.bot.exceptions.ClientNotFoundException;
import ru.akvine.marketspace.bot.exceptions.ClientWhitelistException;
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
    @Value("${client.uuid.length}")
    private int length;
    @Value("${whitelist.usernames}")
    private List<String> whitelistUsernames;

    private final ClientRepository clientRepository;
    private final BlockingService blockingService;

    @Nullable
    public ClientBean findByChatId(String chatId) {
        Preconditions.checkNotNull(chatId, "chatId is null");

        Optional<ClientEntity> client = clientRepository.findByChatId(chatId);
        if (client.isEmpty()) {
            return null;
        }
        return new ClientBean(client.get());
    }

    public ClientBean create(ClientCreate clientCreate) {
        Preconditions.checkNotNull(clientCreate, "ClientCreate is null");

        ClientEntity clientEntity = new ClientEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes(length))
                .setChatId(clientCreate.getChatId())
                .setUsername(clientCreate.getUsername())
                .setFirstName(clientCreate.getFirstName())
                .setLastName(clientCreate.getLastName());

        return new ClientBean(clientRepository.save(clientEntity));
    }

    public void checkIsBlockedAndThrowException(String clientUuid) {
        LocalDateTime blockDateTime = blockingService.getEndBlockDate(clientUuid);
        if (blockDateTime != null) {
            String errorMessage = String.format("Вы были заблокированы до %s!", blockDateTime.toLocalDate());
            throw new BlockedCredentialsException(errorMessage);
        }
    }

    public void checkIsInWhitelist(String username) {
        if (!whitelistUsernames.contains(username)) {
            String errorMessage = String.format("Client with username = [%s] not in whitelist", username);
            throw new ClientWhitelistException(errorMessage);
        }
    }

    public void addToWhitelist(String username) {
        logger.info("Add username = {} to whitelist", username);
        verifyExistsByUsername(username);
        if (!whitelistUsernames.contains(username)) {
            whitelistUsernames.add(username);
        }
    }

    public ClientEntity verifyExistsByClientUuid(String clientUuid) {
        Preconditions.checkNotNull(clientUuid, "clientUuid is null");
        return clientRepository.findByUuid(clientUuid).orElseThrow(() -> new ClientNotFoundException("Client has no with uuid = [" + clientUuid + "]!"));
    }

    public ClientBean getByChatId(String chatId) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        return new ClientBean(verifyExistsByChatId(chatId));
    }

    public ClientBean getByUsername(String username) {
        Preconditions.checkNotNull(username, "username is null");
        return new ClientBean(verifyExistsByUsername(username));
    }

    public ClientEntity verifyExistsByChatId(String chatId) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        return clientRepository.findByChatId(chatId).orElseThrow(() -> new ClientNotFoundException("Client has no with chatId = [" + chatId + "]!"));
    }

    public ClientEntity verifyExistsByUsername(String username) {
        Preconditions.checkNotNull(username, "username is null");
        return clientRepository.findByUsername(username).orElseThrow(() -> new ClientNotFoundException("Client has no with username = [" + username + "]!"));
    }

    public List<ClientEntity> getByListChatId(List<String> chatIds) {
        Preconditions.checkNotNull(chatIds, "chatIds is null");
        return clientRepository.findByListChatId(chatIds);
    }

    public List<ClientEntity> getAll() {
        return clientRepository.findAll();
    }
}
