package ru.akvine.wild.bot.services;

import com.google.common.base.Preconditions;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.exceptions.BlockedCredentialsException;
import ru.akvine.wild.bot.exceptions.ClientNotFoundException;
import ru.akvine.wild.bot.repositories.ClientRepository;
import ru.akvine.wild.bot.services.domain.ClientModel;
import ru.akvine.wild.bot.services.dto.ClientCreate;
import ru.akvine.wild.bot.services.dto.ClientUpdate;
import ru.akvine.wild.bot.utils.UUIDGenerator;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {
    private final ClientRepository clientRepository;
    private final ClientBlockingService clientBlockingService;
    private final EncryptionService encryptionService;

    @Nullable
    public ClientModel findByChatIdAndBotType(String chatId, BotType botType) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        Preconditions.checkNotNull(botType, "botType is null");
        logger.debug("Find client by chatId = {} and bot type = {}", chatId, botType);

        Optional<ClientEntity> client = clientRepository.findByChatIdAndBotType(chatId, botType);
        return client.map(ClientModel::new).orElse(null);
    }

    public ClientModel create(ClientCreate clientCreate) {
        Preconditions.checkNotNull(clientCreate, "ClientCreate is null");
        logger.info("Create client = [{}]", clientCreate);

        ClientEntity clientEntity = new ClientEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setChatId(clientCreate.getChatId())
                .setUsername(clientCreate.getUsername())
                .setFirstName(clientCreate.getFirstName())
                .setLastName(clientCreate.getLastName())
                .setBotType(clientCreate.getBotType());

        return new ClientModel(clientRepository.save(clientEntity));
    }

    public ClientModel update(ClientUpdate action) {
        logger.info("Update client with chat id = [{}], bot type = [{}]", action.getChatId(), action.getBotType());

        ClientEntity clientToUpdate = verifyExistsByChatIdAndBotType(action.getChatId(), action.getBotType());

        if (!action.getTokenToUpdate().equals(encryptionService.decrypt(clientToUpdate.getToken()))) {
            clientToUpdate.setToken(encryptionService.encrypt(action.getTokenToUpdate()));
        }

        clientToUpdate.setUpdatedDate(LocalDateTime.now());

        ClientModel updatedClient = new ClientModel(clientRepository.save(clientToUpdate));
        logger.info(
                "Successful update client with chat id = [{}], bot type = [{}]",
                action.getChatId(),
                action.getBotType());
        return updatedClient;
    }

    public boolean revokeToken(String chatId, BotType botType) {
        logger.info("Revoke token for client with chat id = [{}] and bot type = [{}]", chatId, botType);

        ClientEntity clientToRevokeToken = verifyExistsByChatIdAndBotType(chatId, botType);
        clientToRevokeToken.setToken(null);

        ClientEntity updatedClient = clientRepository.save(clientToRevokeToken);
        if (updatedClient.getToken() != null) {
            logger.debug("Revoke token for client with chat id = [{}] and bot type = [{}] failed!", chatId, botType);
            return false;
        }

        return true;
    }

    // TODO: добавить шедулер для проверки истечения блокировки
    public void checkIsBlocked(String chatId, BotType botType) {
        logger.debug("Check client is blocked by chat id = {}", chatId);
        LocalDateTime blockDateTime = clientBlockingService.getEndBlockDate(chatId, botType);
        if (blockDateTime != null) {
            String errorMessage = String.format(
                    "Client with chat id = [%s], bot type = [%s] has blocked until = [%s]!",
                    chatId, botType, blockDateTime);
            throw new BlockedCredentialsException(errorMessage, blockDateTime.toLocalDate());
        }
    }

    public ClientEntity verifyExistsByClientUuid(String clientUuid) {
        Preconditions.checkNotNull(clientUuid, "clientUuid is null");
        logger.info("Verify client exists by uuid = {}", clientUuid);
        return clientRepository
                .findByUuid(clientUuid)
                .orElseThrow(() -> new ClientNotFoundException("Client has no with uuid = [" + clientUuid + "]!"));
    }

    public ClientModel getByChatIdAndBotType(String chatId, BotType botType) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        logger.debug("Get client by uuid = {}", chatId);
        return new ClientModel(verifyExistsByChatIdAndBotType(chatId, botType));
    }

    public ClientEntity verifyExistsByChatIdAndBotType(String chatId, BotType botType) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        logger.debug("Verify client exists by chat with id = {}", chatId);
        return clientRepository
                .findByChatIdAndBotType(chatId, botType)
                .orElseThrow(() -> new ClientNotFoundException(
                        "Client has no with chatId = [" + chatId + "] and bot type = [" + botType + "]!"));
    }

    public ClientEntity verifyExistsByUsername(String username) {
        Preconditions.checkNotNull(username, "username is null");
        logger.info("Verify client exists by username = {}", username);
        return clientRepository
                .findByUsername(username)
                .orElseThrow(() -> new ClientNotFoundException("Client has no with username = [" + username + "]!"));
    }

    public List<ClientModel> getByListChatId(List<String> chatIds) {
        Preconditions.checkNotNull(chatIds, "chatIds is null");
        logger.info("Get by chat ids = {}", chatIds);
        return clientRepository.findByListChatId(chatIds).stream()
                .map(ClientModel::new)
                .toList();
    }

    public List<ClientModel> getAll() {
        logger.info("Get all clients");
        return clientRepository.findAll().stream().map(ClientModel::new).toList();
    }

    public List<ClientModel> getAllActive() {
        logger.info("Get all active clients");
        return clientRepository.findAllActive().stream()
                .map(ClientModel::new)
                .toList();
    }

    public List<ClientModel> getAllByUsernames(List<String> usernames) {
        logger.info("Get all clients by usernames = {}", usernames);
        return clientRepository.findByUsernames(usernames).stream()
                .map(ClientModel::new)
                .toList();
    }
}
