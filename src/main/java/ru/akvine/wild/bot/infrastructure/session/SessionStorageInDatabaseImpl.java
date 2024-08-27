package ru.akvine.wild.bot.infrastructure.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import ru.akvine.wild.bot.entities.infrastructure.ClientSessionDataEntity;
import ru.akvine.wild.bot.infrastructure.exceptions.NoSessionException;
import ru.akvine.wild.bot.repositories.infrastructure.ClientSessionDataRepository;

@RequiredArgsConstructor
@Slf4j
public class SessionStorageInDatabaseImpl implements SessionStorage<String, ClientSessionData> {
    private final ClientSessionDataRepository clientSessionDataRepository;

    @Override
    public void init(String chatId) {
        if (!hasSession(chatId)) {
            logger.debug("Init database session for chat id = {}", chatId);
            ClientSessionDataEntity session = new ClientSessionDataEntity().setChatId(chatId);
            clientSessionDataRepository.save(session);
        }
    }

    @Override
    public ClientSessionData get(String chatId) {
        logger.debug("Get database session for chat id = {}", chatId);
        return new ClientSessionData(verifyExistsAndGet(chatId));
    }

    @Override
    public ClientSessionData save(ClientSessionData data) {
        ClientSessionDataEntity session = verifyExistsAndGet(data.getChatId());
        session
                .setSelectedCardType(data.getSelectedCardType())
                .setSelectedCategoryId(data.getSelectedCategoryId())
                .setUploadedCardPhoto(data.getUploadedCardPhoto())
                .setNewCardPrice(data.getNewCardPrice())
                .setNewCardDiscount(data.getNewCardDiscount())
                .setLockedAdvertId(data.getLockedAdvertId());
        return new ClientSessionData(clientSessionDataRepository.save(session));
    }

    @Override
    public boolean hasSession(String chatId) {
        try {
            get(chatId);
            return true;
        } catch (NoSessionException exception) {
            return false;
        }
    }

    @Override
    @Transactional
    public void close(String chatId) {
        ClientSessionDataEntity session = verifyExistsAndGet(chatId);
        clientSessionDataRepository.delete(session);
    }

    private ClientSessionDataEntity verifyExistsAndGet(String chatId) {
        return clientSessionDataRepository
                .findByChatId(chatId)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Chat id = [%s] has no database session", chatId);
                    return new NoSessionException(errorMessage);
                });
    }
}
