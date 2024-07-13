package ru.akvine.marketspace.bot.admin.converters;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.akvine.marketspace.bot.admin.dto.client.*;
import ru.akvine.marketspace.bot.services.dto.admin.client.*;
import ru.akvine.marketspace.bot.utils.DateUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClientConverter {
    private static final int BLOCK_TIME_YEARS = 100;

    public BlockClientStart convertToBlockClientStart(BlockClientRequest request) {
        Preconditions.checkNotNull(request, "blockClientRequest is null");
        BlockClientStart start = new BlockClientStart();
        start.setUuid(request.getUuid());

        if (request.getDate() == null) {
            start.setMinutes(DateUtils.getMinutes(BLOCK_TIME_YEARS));
        } else {
            start.setMinutes(LocalDateTime.now().until(request.getDate(), ChronoUnit.MINUTES));
        }

        if (StringUtils.isNotBlank(request.getUuid())) {
            start.setUuid(request.getUuid());
        }
        if (StringUtils.isNotBlank(request.getChatId())) {
            start.setChatId(request.getChatId());
        }
        if (StringUtils.isNotBlank(request.getUsername())) {
            start.setUsername(request.getUsername());
        }

        return start;
    }

    public BlockClientResponse convertToBlockClientResponse(BlockClientFinish finish) {
        Preconditions.checkNotNull(finish, "blockClientFinish is null");
        return new BlockClientResponse()
                .setUuid(finish.getUuid())
                .setDateTime(finish.getDateTime())
                .setMinutes(finish.getMinutes());
    }

    public ListBlockClientResponse convertToListBlockClientResponse(List<BlockClientEntry> blocked) {
        Preconditions.checkNotNull(blocked, "blockedClientEntries is null");
        return new ListBlockClientResponse()
                .setCount(blocked.size())
                .setList(blocked
                        .stream()
                        .map(this::buildBlockClientDto)
                        .collect(Collectors.toList()));
    }

    public UnblockClient convertToUnblockClient(UnblockClientRequest request) {
        Preconditions.checkNotNull(request, "unblockClientRequest");
        UnblockClient unblockClient = new UnblockClient();

        if (StringUtils.isNotBlank(request.getUuid())) {
            unblockClient.setUuid(request.getUuid());
        }
        if (StringUtils.isNotBlank(request.getChatId())) {
            unblockClient.setChatId(request.getChatId());
        }
        if (StringUtils.isNotBlank(request.getUsername())) {
            unblockClient.setUsername(request.getUsername());
        }

        return unblockClient;
    }

    public SendMessage convertToSendMessage(SendMessageRequest request) {
        Preconditions.checkNotNull(request, "SendMessageRequest is null");
        return new SendMessage()
                .setMessage(request.getMessage())
                .setChatIds(request.getChatIds())
                .setUsernames(request.getUsernames());
    }

    public AddToWhitelist convertToAddToWhitelist(AddToWhitelistRequest request) {
        Preconditions.checkNotNull(request, "addToWhitelistRequest is null");
        return new AddToWhitelist()
                .setChatId(request.getChatId())
                .setUsername(request.getUsername());
    }

    private BlockClientDto buildBlockClientDto(BlockClientEntry entry) {
        return new BlockClientDto()
                .setUuid(entry.getUuid())
                .setMinutes(entry.getMinutes())
                .setBlockStartDate(entry.getBlockStartDate())
                .setBlockEndDate(entry.getBlockEndDate());
    }
}
