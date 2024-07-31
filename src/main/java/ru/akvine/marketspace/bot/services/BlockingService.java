package ru.akvine.marketspace.bot.services;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.entities.BlockedCredentialsEntity;
import ru.akvine.marketspace.bot.exceptions.BlockedCredentialsException;
import ru.akvine.marketspace.bot.repositories.BlockedCredentialsRepository;
import ru.akvine.marketspace.bot.utils.DateUtils;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockingService {
    private final BlockedCredentialsRepository blockedCredentialsRepository;

    public void setBlock(String uuid, Long minutes) {
        logger.info("Block client with uuid = {} for minutes = {}", uuid, minutes);
        Optional<BlockedCredentialsEntity> blockedCredentialsOptional = blockedCredentialsRepository.findByUuid(uuid);
        if (blockedCredentialsOptional.isPresent()) {
            DateUtils.getMinutes(
                    blockedCredentialsOptional.get().getBlockStartDate(),
                    blockedCredentialsOptional.get().getBlockEndDate()
            );
            return;
        }

        BlockTime newBlock = new BlockTime(minutes);
        BlockedCredentialsEntity newBlockedCredentials = new BlockedCredentialsEntity();
        newBlockedCredentials.setUuid(uuid);
        newBlockedCredentials.setBlockStartDate(newBlock.start);
        newBlockedCredentials.setBlockEndDate(newBlock.end);

        blockedCredentialsRepository.save(newBlockedCredentials);
    }

    public void removeBlock(String uuid) {
        logger.info("Remove block client with uuid = {}", uuid);
        BlockedCredentialsEntity blockedCredentialsEntity = blockedCredentialsRepository
                .findByUuid(uuid)
                .orElseThrow(() -> new BlockedCredentialsException("Not exists block record for client with uuid = [" + uuid + "]"));

        blockedCredentialsRepository.delete(blockedCredentialsEntity);
    }

    public List<BlockedCredentialsEntity> list() {
        logger.info("List blocked clients");
        return blockedCredentialsRepository.findAll();
    }

    @Nullable
    public LocalDateTime getEndBlockDate(String clientUuid) {
        Preconditions.checkNotNull(clientUuid, "clientUuid is null");
        logger.debug("Get end block date for client with uuid = {}", clientUuid);
        Optional<BlockedCredentialsEntity> blockedCredentialsEntityOptional = blockedCredentialsRepository.findByUuid(clientUuid);
        return blockedCredentialsEntityOptional.map(BlockedCredentialsEntity::getBlockEndDate).orElse(null);
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
