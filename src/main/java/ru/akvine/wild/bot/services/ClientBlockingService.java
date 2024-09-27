package ru.akvine.wild.bot.services;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.entities.ClientBlockedCredentialsEntity;
import ru.akvine.wild.bot.repositories.ClientBlockedCredentialsRepository;
import ru.akvine.wild.bot.utils.DateUtils;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientBlockingService {
    private final ClientBlockedCredentialsRepository clientBlockedCredentialsRepository;

    public void setBlock(String chatId, Long minutes) {
        logger.info("Block client with chatId = {} for minutes = {}", chatId, minutes);
        Optional<ClientBlockedCredentialsEntity> blockedCredentialsOptional = clientBlockedCredentialsRepository.findByChatId(chatId);
        if (blockedCredentialsOptional.isPresent()) {
            DateUtils.getMinutes(
                    blockedCredentialsOptional.get().getBlockStartDate(),
                    blockedCredentialsOptional.get().getBlockEndDate()
            );
            return;
        }

        BlockTime newBlock = new BlockTime(minutes);
        ClientBlockedCredentialsEntity newBlockedCredentials = new ClientBlockedCredentialsEntity();
        newBlockedCredentials.setChatId(chatId);
        newBlockedCredentials.setBlockStartDate(newBlock.start);
        newBlockedCredentials.setBlockEndDate(newBlock.end);

        clientBlockedCredentialsRepository.save(newBlockedCredentials);
    }

    public void removeBlock(String chatId) {
        logger.info("Remove block client with chatId = {}", chatId);
        ClientBlockedCredentialsEntity clientBlockedCredentialsEntity = clientBlockedCredentialsRepository
                .findByChatId(chatId)
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
    public LocalDateTime getEndBlockDate(String clientUuid) {
        Preconditions.checkNotNull(clientUuid, "clientUuid is null");
        logger.debug("Get end block date for client with uuid = {}", clientUuid);
        Optional<ClientBlockedCredentialsEntity> blockedCredentialsEntityOptional = clientBlockedCredentialsRepository.findByChatId(clientUuid);
        return blockedCredentialsEntityOptional.map(ClientBlockedCredentialsEntity::getBlockEndDate).orElse(null);
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
