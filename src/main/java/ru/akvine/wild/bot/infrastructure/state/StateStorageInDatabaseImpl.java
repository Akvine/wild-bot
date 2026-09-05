package ru.akvine.wild.bot.infrastructure.state;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.akvine.wild.bot.entities.infrastructure.ClientStatesEntity;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.repositories.infrastructure.ClientStatesRepository;

@RequiredArgsConstructor
@Slf4j
public class StateStorageInDatabaseImpl implements StateStorage<String, List<ClientState>> {
    private final ClientStatesRepository clientStatesRepository;

    @Override
    public void add(String chatId, BotType botType, ClientState state) {
        String uniqueIdentifier = createUniqueIdentifier(chatId, botType);

        Optional<ClientStatesEntity> optionalClientStates = clientStatesRepository.findByIdentifier(uniqueIdentifier);
        if (optionalClientStates.isPresent()) {
            ClientStatesEntity clientStatesEntity = optionalClientStates.get();
            clientStatesEntity.getStates().add(state);
            clientStatesEntity.setUpdatedDate(LocalDateTime.now());
            clientStatesRepository.save(clientStatesEntity);
        } else {
            ClientStatesEntity statesStackEntity = new ClientStatesEntity()
                    .setIdentifier(uniqueIdentifier)
                    .setStates(new ArrayList<>(Arrays.asList(state)));
            clientStatesRepository.save(statesStackEntity);
        }
    }

    @Override
    public boolean containsState(String chatId, BotType botType) {
        return clientStatesRepository
                .findByIdentifier(createUniqueIdentifier(chatId, botType))
                .isPresent();
    }

    @Override
    public ClientState getCurrent(String chatId, BotType botType) {
        String uniqueIdentifier = createUniqueIdentifier(chatId, botType);
        Optional<ClientStatesEntity> optionalClientStates = clientStatesRepository.findByIdentifier(uniqueIdentifier);
        if (optionalClientStates.isPresent()) {
            return optionalClientStates.get().getStates().getLast();
        }

        String errorMessage =
                String.format("Has no found client states by identifier = [%s]. Can't get current!", uniqueIdentifier);
        throw new IllegalStateException(errorMessage);
    }

    @Override
    public void removeCurrent(String chatId, BotType botType) {
        String uniqueIdentifier = createUniqueIdentifier(chatId, botType);
        Optional<ClientStatesEntity> optionalClientStates = clientStatesRepository.findByIdentifier(uniqueIdentifier);
        if (optionalClientStates.isPresent()) {
            ClientStatesEntity entity = optionalClientStates.get();
            entity.getStates().removeLast();
            entity.setUpdatedDate(LocalDateTime.now());
            clientStatesRepository.save(entity);
            return;
        }

        String errorMessage =
                String.format("Has no found client states by identifier = [%s]. Can't get current!", uniqueIdentifier);
        throw new IllegalStateException(errorMessage);
    }

    @Override
    public ClientState removeCurrentAndGetPrevious(String chatId, BotType botType) {
        removeCurrent(chatId, botType);
        return getCurrent(chatId, botType);
    }

    @Override
    public void close(String chatId, BotType botType) {
        String uniqueIdentifier = createUniqueIdentifier(chatId, botType);
        Optional<ClientStatesEntity> optionalClientStates = clientStatesRepository.findByIdentifier(uniqueIdentifier);
        optionalClientStates.ifPresent(clientStatesRepository::delete);
    }

    @Override
    public int statesCount(String chatId, BotType botType) {
        String uniqueIdentifier = createUniqueIdentifier(chatId, botType);
        Optional<ClientStatesEntity> optionalClientStates = clientStatesRepository.findByIdentifier(uniqueIdentifier);
        return optionalClientStates
                .map(clientStatesEntity -> clientStatesEntity.getStates().size())
                .orElse(0);
    }

    private String createUniqueIdentifier(String chatId, BotType botType) {
        return chatId + "_" + botType;
    }
}
