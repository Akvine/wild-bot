package ru.akvine.wild.bot.services;

import com.google.common.base.Preconditions;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.entities.ClientBlockedCredentialsEntity;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.repositories.ClientBlockedCredentialsRepository;
import ru.akvine.wild.bot.utils.DateUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientBlockingService {
    private final ClientBlockedCredentialsRepository clientBlockedCredentialsRepository;

    public void setBlock(String chatId, BotType botType, Long minutes) {
        logger.info("Block client with chatId = {} and bot type = {} for minutes = {}", chatId, botType, minutes);
        Optional<ClientBlockedCredentialsEntity> blockedCredentialsOptional =
                clientBlockedCredentialsRepository.findByChatIdAndBotType(chatId, botType);
        if (blockedCredentialsOptional.isPresent()) {
            DateUtils.getMinutes(
                    blockedCredentialsOptional.get().getBlockStartDate(),
                    blockedCredentialsOptional.get().getBlockEndDate());
            return;
        }

        BlockTime newBlock = new BlockTime(minutes);
        ClientBlockedCredentialsEntity newBlockedCredentials = new ClientBlockedCredentialsEntity();
        newBlockedCredentials.setChatId(chatId);
        newBlockedCredentials.setBlockStartDate(newBlock.start);
        newBlockedCredentials.setBlockEndDate(newBlock.end);
        newBlockedCredentials.setBotType(botType);

        clientBlockedCredentialsRepository.save(newBlockedCredentials);
    }

    public void removeBlock(String chatId, BotType botType) {
        logger.info("Remove block client with chatId = {} and bot type = {}", chatId, botType);
        ClientBlockedCredentialsEntity clientBlockedCredentialsEntity = clientBlockedCredentialsRepository
                .findByChatIdAndBotType(chatId, botType)
                .orElse(null);
        if (clientBlockedCredentialsEntity == null) {
            return;
        }

        clientBlockedCredentialsRepository.delete(clientBlockedCredentialsEntity);
    }

    public List<ClientBlockedCredentialsEntity> list() {
        logger.info("List blocked clients");
        return clientBlockedCredentialsRepository.findAll();
    }

    @Nullable
    public LocalDateTime getEndBlockDate(String chatId, BotType botType) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        logger.debug("Get end block date for client with chatId = {} and bot type = {}", chatId, botType);
        Optional<ClientBlockedCredentialsEntity> blockedCredentialsEntityOptional =
                clientBlockedCredentialsRepository.findByChatIdAndBotType(chatId, botType);
        return blockedCredentialsEntityOptional
                .map(ClientBlockedCredentialsEntity::getBlockEndDate)
                .orElse(null);
    }

    @ThreadSafe
    public static class BlockTime {
        final LocalDateTime start;
        final LocalDateTime end;

        public BlockTime(long howMuchMinutes) {
            this.start = LocalDateTime.now();
            this.end = start.plusMinutes(howMuchMinutes);
        }
    }
}
