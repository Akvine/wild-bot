package ru.akvine.wild.bot.infrastructure.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import ru.akvine.wild.bot.entities.infrastructure.ClientSessionDataEntity;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.infrastructure.exceptions.NoSessionException;
import ru.akvine.wild.bot.repositories.infrastructure.ClientSessionDataRepository;

/**
 * Реализация {@link SessionStorage} поверх БД ({@link ClientSessionDataRepository}) — сессия
 * переживает рестарт приложения, в отличие от {@link SessionStorageInMemoryImpl}.
 */
@RequiredArgsConstructor
@Slf4j
public class SessionStorageInDatabaseImpl implements SessionStorage<String, ClientSessionData> {
    private final ClientSessionDataRepository clientSessionDataRepository;

    @Override
    public void init(String chatId, BotType botType) {
        if (!hasSession(chatId, botType)) {
            logger.debug("Init database session for chat id = {} and bot type = {}", chatId, botType);
            ClientSessionDataEntity session =
                    new ClientSessionDataEntity().setChatId(chatId).setBotType(botType);
            clientSessionDataRepository.save(session);
        }
    }

    @Override
    public ClientSessionData get(String chatId, BotType botType) {
        logger.debug("Get database session for chat id = {} and bot type = {}", chatId, botType);
        return new ClientSessionData(verifyExistsAndGet(chatId, botType));
    }

    @Override
    public ClientSessionData save(ClientSessionData data, BotType botType) {
        ClientSessionDataEntity session = verifyExistsAndGet(data.getChatId(), data.getBotType());
        session.setSelectedCardType(data.getSelectedCardType())
                .setBotType(botType)
                .setSelectedCategoryId(data.getSelectedCategoryId())
                .setUploadedCardPhoto(data.getUploadedCardPhoto())
                .setInputNewCardPriceAndDiscount(data.isInputNewCardPriceAndDiscount())
                .setNewCardPrice(data.getNewCardPrice())
                .setNewCardDiscount(data.getNewCardDiscount())
                .setAdvertIdToStart(data.getAdvertIdToStart());
        return new ClientSessionData(clientSessionDataRepository.save(session));
    }

    @Override
    public boolean hasSession(String chatId, BotType botType) {
        try {
            get(chatId, botType);
            return true;
        } catch (NoSessionException exception) {
            return false;
        }
    }

    @Override
    @Transactional
    public void close(String chatId, BotType botType) {
        ClientSessionDataEntity session = verifyExistsAndGet(chatId, botType);
        clientSessionDataRepository.delete(session);
    }

    private ClientSessionDataEntity verifyExistsAndGet(String chatId, BotType botType) {
        return clientSessionDataRepository
                .findByChatIdAndBotType(chatId, botType)
                .orElseThrow(() -> {
                    String errorMessage = String.format("Chat id = [%s] has no database session", chatId);
                    return new NoSessionException(errorMessage);
                });
    }
}
