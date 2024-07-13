package ru.akvine.marketspace.bot.services.admin;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.akvine.marketspace.bot.entities.BlockedCredentialsEntity;
import ru.akvine.marketspace.bot.exceptions.ClientNotFoundException;
import ru.akvine.marketspace.bot.services.BlockingService;
import ru.akvine.marketspace.bot.services.ClientService;
import ru.akvine.marketspace.bot.services.domain.ClientBean;
import ru.akvine.marketspace.bot.services.dto.admin.client.*;
import ru.akvine.marketspace.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.marketspace.bot.utils.DateUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientAdminService {
    private final BlockingService blockingService;
    private final ClientService clientService;
    private final TelegramIntegrationService telegramIntegrationService;

    public BlockClientFinish blockClient(BlockClientStart start) {
        Preconditions.checkNotNull(start, "blockClientStart is null");
        long minutes = start.getMinutes();
        String uuid;

        if (StringUtils.isNotBlank(start.getUuid())) {
            clientService.verifyExistsByClientUuid(start.getUuid());
            uuid = start.getUuid();
        } else if (StringUtils.isNotBlank(start.getChatId())) {
            uuid = clientService.verifyExistsByChatId(start.getChatId()).getUuid();
        } else {
            uuid = clientService.verifyExistsByUsername(start.getUsername()).getUuid();
        }

        LocalDateTime blockDate = LocalDateTime.now().plusMinutes(minutes);
        blockingService.setBlock(uuid, minutes);

        return new BlockClientFinish()
                .setUuid(uuid)
                .setDateTime(blockDate)
                .setMinutes(minutes);
    }

    public List<BlockClientEntry> listBlocked() {
        List<BlockedCredentialsEntity> list = blockingService.list();

        return list.stream().map(obj -> {
            LocalDateTime start = obj.getBlockStartDate();
            LocalDateTime end = obj.getBlockEndDate();
            String uuid = obj.getUuid();
            long minutes = DateUtils.getMinutes(start, end);
            return new BlockClientEntry()
                    .setUuid(uuid)
                    .setBlockStartDate(start)
                    .setBlockEndDate(end)
                    .setMinutes(minutes);
        }).collect(Collectors.toList());
    }

    public void unblockClient(UnblockClient unblockClient) {
        Preconditions.checkNotNull(unblockClient, "unblockClient is null");
        String uuid;

        if (StringUtils.isNotBlank(unblockClient.getUuid())) {
            clientService.verifyExistsByClientUuid(unblockClient.getUuid());
            uuid = unblockClient.getUuid();
        } else if (StringUtils.isNotBlank(unblockClient.getChatId())) {
            uuid = clientService.verifyExistsByChatId(unblockClient.getChatId()).getUuid();
        } else {
            uuid = clientService.verifyExistsByUsername(unblockClient.getUsername()).getUuid();
        }

        blockingService.removeBlock(uuid);
    }

    public void sendMessage(SendMessage sendMessage) {
        Preconditions.checkNotNull(sendMessage, "sendMessage is null");
        logger.info("Send message by request = {}", sendMessage);

        String message = sendMessage.getMessage();
        List<ClientBean> activeClients;
        if (!CollectionUtils.isEmpty(sendMessage.getChatIds())) {
            activeClients = clientService.getByListChatId(sendMessage.getChatIds());
            if (CollectionUtils.isEmpty(activeClients)) {
                String errorMessage = String.format("Not found any client with chat ids = %s", sendMessage.getChatIds());
                throw new ClientNotFoundException(errorMessage);
            }

            sendMessageInternal(activeClients, message);
            return;
        }

        if (!CollectionUtils.isEmpty(sendMessage.getUsernames())) {
            activeClients = clientService.getAllByUsernames(sendMessage.getUsernames());
            if (CollectionUtils.isEmpty(activeClients)) {
                String errorMessage = String.format("Not found any client with usernames = %s", sendMessage.getUsernames());
                throw new ClientNotFoundException(errorMessage);
            }

            sendMessageInternal(activeClients, message);
            return;
        }

        activeClients = clientService.getAll();
        sendMessageInternal(activeClients, message);
    }

    private void sendMessageInternal(List<ClientBean> activeClients, String message) {
        List<String> activeChatIds = activeClients
                .stream()
                .map(ClientBean::getChatId)
                .collect(Collectors.toList());
        telegramIntegrationService.sendMessage(activeChatIds, message);
    }

    public void addToWhiteList(AddToWhitelist addToWhitelist) {
        Preconditions.checkNotNull(addToWhitelist, "addToWhiteList is null");
        String username;
        if (StringUtils.isNotBlank(addToWhitelist.getChatId())) {
            username = clientService
                    .getByChatId(addToWhitelist.getChatId())
                    .getUsername();
        } else {
            username = addToWhitelist.getUsername();
        }

        clientService.addToWhitelist(username);
    }
}
