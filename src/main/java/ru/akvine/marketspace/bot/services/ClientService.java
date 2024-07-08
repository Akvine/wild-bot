package ru.akvine.marketspace.bot.services;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.entities.ClientEntity;
import ru.akvine.marketspace.bot.exceptions.ClientNotFoundException;
import ru.akvine.marketspace.bot.repositories.ClientRepository;
import ru.akvine.marketspace.bot.services.domain.ClientBean;
import ru.akvine.marketspace.bot.services.dto.ClientCreate;
import ru.akvine.marketspace.bot.utils.UUIDGenerator;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientService {
    @Value("${client.uuid.length}")
    private int length;

    private final ClientRepository clientRepository;

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

    public ClientEntity verifyExistsByClientUuid(String clientUuid) {
        Preconditions.checkNotNull(clientUuid, "clientUuid is null");
        return clientRepository.findByUuid(clientUuid).orElseThrow(() -> new ClientNotFoundException("Client has no with uuid = [" + clientUuid + "]!"));
    }

    public ClientBean getByChatId(String chatId) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        return new ClientBean(verifyExistsByChatId(chatId));
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
