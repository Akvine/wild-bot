package ru.akvine.marketspace.bot.services.admin;

import com.google.common.base.Preconditions;
import io.nayuki.qrcodegen.QrCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.akvine.marketspace.bot.entities.BlockedCredentialsEntity;
import ru.akvine.marketspace.bot.entities.ClientEntity;
import ru.akvine.marketspace.bot.exceptions.ClientNotFoundException;
import ru.akvine.marketspace.bot.repositories.ClientRepository;
import ru.akvine.marketspace.bot.services.BlockingService;
import ru.akvine.marketspace.bot.services.ClientService;
import ru.akvine.marketspace.bot.services.domain.ClientModel;
import ru.akvine.marketspace.bot.services.dto.admin.client.*;
import ru.akvine.marketspace.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.marketspace.bot.utils.DateUtils;
import ru.akvine.marketspace.bot.utils.QrCodeUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientAdminService {
    private final BlockingService blockingService;
    private final ClientService clientService;
    // TODO : лучше делать обновление сущности в ClientService, так по канону
    private final ClientRepository clientRepository;
    private final TelegramIntegrationService telegramIntegrationService;

    public List<ClientModel> list() {
        return clientService.getAll();
    }

    public ClientModel addTestsToClient(AddTests addTests) {
        ClientEntity client;
        if (StringUtils.isNotBlank(addTests.getUsername())) {
            client = clientService.verifyExistsByUsername(addTests.getUsername());
        } else {
            client = clientService.verifyExistsByChatId(addTests.getChatId());
        }

        client.increaseAvailableTestsCount(addTests.getTestsCount());
        return new ClientModel(clientRepository.save(client));
    }

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
        List<ClientModel> activeClients;
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

    public void addToWhitelist(Whitelist whitelist) {
        Preconditions.checkNotNull(whitelist, "whitelist is null");
        logger.info("Add client to whitelist by {}", whitelist);

        ClientEntity client;
        if (StringUtils.isNotBlank(whitelist.getChatId())) {
            client = clientService.verifyExistsByChatId(whitelist.getChatId());
        } else {
            client = clientService.verifyExistsByUsername(whitelist.getUsername());
        }

        client.setInWhitelist(true);
        clientRepository.save(client);
        logger.info("Successful add to whitelist client with chatId = {} and username = {}", client.getChatId(), client.getUsername());
    }

    public void deleteFromWhitelist(Whitelist whitelist) {
        Preconditions.checkNotNull(whitelist, "whitelist is null");
        logger.info("Delete client from whitelist by {}", whitelist);

        ClientEntity client;
        if (StringUtils.isNotBlank(whitelist.getChatId())) {
            client = clientService.verifyExistsByChatId(whitelist.getChatId());
        } else {
            client = clientService.verifyExistsByUsername(whitelist.getUsername());
        }

        client.setInWhitelist(false);
        clientRepository.save(client);
        logger.info("Successful delete to whitelist client with chatId = {} and username = {}", client.getChatId(), client.getUsername());
    }

    public void sendQrCode(String chatId, String text, String caption) {
        logger.info("Send qr code to chat with id = {} and text = {}", chatId, text);
        clientService.verifyExistsByChatId(chatId);
        QrCode qrCode = QrCode.encodeText(text, QrCode.Ecc.HIGH);
        byte[] image = QrCodeUtils.convertQrCodeToBytes(qrCode);
        telegramIntegrationService.sendImage(chatId, image, caption);
    }

    private void sendMessageInternal(List<ClientModel> activeClients, String message) {
        List<String> activeChatIds = activeClients
                .stream()
                .map(ClientModel::getChatId)
                .collect(Collectors.toList());
        telegramIntegrationService.sendMessage(activeChatIds, message);
    }
}
