package ru.akvine.marketspace.bot.infrastructure.state;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.transaction.annotation.Transactional;
import ru.akvine.marketspace.bot.entities.infrastructure.StateEntity;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.repositories.infrastructure.StateRepository;

@RequiredArgsConstructor
@Slf4j
public class StateStorageInDatabaseImpl implements StateStorage<String> {
    private final StateRepository stateRepository;

    @Override
    public void setState(String chatId, ClientState state) {
        ClientState clientState = getState(chatId);
        if (clientState == null) {
            StateEntity stateEntity = new StateEntity()
                    .setChatId(chatId)
                    .setClientState(state);
            stateRepository.save(stateEntity);
        } else {
            StateEntity stateEntity = getByChatId(chatId).setClientState(state);
            stateRepository.save(stateEntity);
        }
    }

    @Override
    public boolean containsState(String chatId) {
        return getState(chatId) != null;
    }

    @Override
    @Nullable
    public ClientState getState(String chatId) {
        StateEntity stateEntity = stateRepository
                .findByChatId(chatId)
                .orElse(null);
        if (stateEntity == null) {
            return null;
        }
        return stateEntity.getClientState();
    }

    @Override
    @Transactional
    public void removeState(String chatId) {
        stateRepository.findByChatId(chatId).ifPresent(stateRepository::delete);
    }

    private StateEntity getByChatId(String chatId) {
        return stateRepository.findByChatId(chatId).orElse(null);
    }
}
