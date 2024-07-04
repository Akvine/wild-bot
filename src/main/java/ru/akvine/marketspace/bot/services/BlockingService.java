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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockingService {
    private final BlockedCredentialsRepository blockedCredentialsRepository;

    public long setBlock(String uuid, Long minutes) {
        Optional<BlockedCredentialsEntity> blockedCredentialsOptional = blockedCredentialsRepository.findByLogin(uuid);
        if (blockedCredentialsOptional.isPresent()) {
            return DateUtils.getMinutes(
                    blockedCredentialsOptional.get().getBlockStartDate(),
                    blockedCredentialsOptional.get().getBlockEndDate()
            );
        }

        BlockTime newBlock = new BlockTime(minutes);
        BlockedCredentialsEntity newBlockedCredentials = new BlockedCredentialsEntity();
        newBlockedCredentials.setUuid(uuid);
        newBlockedCredentials.setBlockStartDate(newBlock.start);
        newBlockedCredentials.setBlockEndDate(newBlock.end);

        BlockedCredentialsEntity savedBlockedCredentials = blockedCredentialsRepository.save(newBlockedCredentials);
        return savedBlockedCredentials.getId();
    }

    public boolean removeBlock(String uuid) {
        BlockedCredentialsEntity blockedCredentialsEntity = blockedCredentialsRepository
                .findByLogin(uuid)
                .orElseThrow(() -> new BlockedCredentialsException("Not exists block record for client with uuid = [" + uuid + "]"));

        blockedCredentialsRepository.delete(blockedCredentialsEntity);
        return true;
    }

    public List<BlockedCredentialsEntity> list() {
        return blockedCredentialsRepository.findAll();
    }

    public boolean isBlocked(String uuid) {
        Preconditions.checkNotNull(uuid, "clientUuid is null");
        Optional<BlockedCredentialsEntity> blockedCredentialsEntityOptional = blockedCredentialsRepository.findByLogin(uuid);
        return blockedCredentialsEntityOptional.filter(blockedCredentialsEntity -> !blockedCredentialsEntity.getBlockEndDate().isAfter(LocalDateTime.now())).isPresent();
    }

    @Nullable
    public LocalDateTime getEndBlockDate(String clientUuid) {
        Preconditions.checkNotNull(clientUuid, "clientUuid is null");
        Optional<BlockedCredentialsEntity> blockedCredentialsEntityOptional = blockedCredentialsRepository.findByLogin(clientUuid);

        if (blockedCredentialsEntityOptional.isEmpty()) {
            return null;
        }
        return blockedCredentialsEntityOptional.get().getBlockEndDate();
    }

    @ThreadSafe
    public static class BlockTime {
        final LocalDateTime start;
        final LocalDateTime end;

        public BlockTime(BlockedCredentialsEntity blockedCredentialsEntity) {
            this.start = blockedCredentialsEntity.getBlockStartDate();
            this.end = blockedCredentialsEntity.getBlockEndDate();
        }

        public BlockTime(long howMuchMinutes) {
            this.start = LocalDateTime.now();
            this.end = start.plus(howMuchMinutes, ChronoUnit.MINUTES);
        }
    }
}
