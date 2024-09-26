package ru.akvine.wild.bot.services.admin;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.akvine.wild.bot.entities.BlockedCredentialsEntity;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.exceptions.ClientNotFoundException;
import ru.akvine.wild.bot.facades.QrCodeGenerationServiceFacade;
import ru.akvine.wild.bot.repositories.ClientRepository;
import ru.akvine.wild.bot.services.ClientBlockingService;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.domain.ClientModel;
import ru.akvine.wild.bot.services.dto.admin.GenerateQrCode;
import ru.akvine.wild.bot.services.dto.admin.client.*;
import ru.akvine.wild.bot.services.integration.qrcode.dto.GenerateQrCodeRequest;
import ru.akvine.wild.bot.services.integration.qrcode.QrCodeGenerationService;
import ru.akvine.wild.bot.services.integration.qrcode.QrCodeGenerationServiceType;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.utils.DateUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientAdminService {
    private final ClientBlockingService clientBlockingService;
    private final ClientService clientService;
    // TODO : лучше делать обновление сущности в ClientService, так по канону
    private final ClientRepository clientRepository;
    private final TelegramIntegrationService telegramIntegrationService;
    private final QrCodeGenerationServiceFacade qrCodeGenerationServiceFacade;

    @Value("${qraft.integration.enabled}")
    private boolean qraftIntegrationEnabled;
    @Value("${qraft.request.param.ecl}")
    private String errorCorrectionLevel;
    @Value("${qraft.request.param.qr.size}")
    private int qrSize;
    @Value("${qraft.request.param.border.size}")
    private int borderSize;
    @Value("${qraft.request.param.radiusFactor}")
    private int radiusFactor;
    @Value("${qraft.request.param.cornerBlockRadiusFactor}")
    private double cornerBlockRadiusFactor;
    @Value("${qraft.request.param.roundInnerCorners}")
    private boolean roundInnerCorners;
    @Value("${qraft.request.param.roundOuterCorners}")
    private boolean roundOuterCorners;
    @Value("${qraft.request.param.cornerBlocksAsCircles}")
    private boolean cornerBlocksAsCircles;
    @Value("${qraft.request.param.image.type}")
    private String imageType;


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
        clientBlockingService.setBlock(uuid, minutes);

        return new BlockClientFinish()
                .setUuid(uuid)
                .setDateTime(blockDate)
                .setMinutes(minutes);
    }

    public List<BlockClientEntry> listBlocked() {
        List<BlockedCredentialsEntity> list = clientBlockingService.list();

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

        clientBlockingService.removeBlock(uuid);
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

    public void sendQrCode(GenerateQrCode generateQrCode) {
        String chatId = generateQrCode.getChatId();
        String url = generateQrCode.getUrl();
        String caption = generateQrCode.getCaption();

        logger.info("Send qr code to chat with id = {} and url = {}", chatId, url);
        clientService.verifyExistsByChatId(chatId);

        Map<QrCodeGenerationServiceType, QrCodeGenerationService> serviceMap = qrCodeGenerationServiceFacade
                .getServicesMap();
        GenerateQrCodeRequest request = new GenerateQrCodeRequest()
                .setUrl(url)
                .setQrSize(qrSize)
                .setBorderSize(borderSize)
                .setRadiusFactor(radiusFactor)
                .setErrorCorrectionLevel(errorCorrectionLevel)
                .setCornerBlockRadiusFactor(cornerBlockRadiusFactor)
                .setRoundInnerCorners(roundInnerCorners)
                .setRoundOuterCorners(roundOuterCorners)
                .setCornerBlocksAsCircles(cornerBlocksAsCircles)
                .setImageType(imageType);

        byte[] image;
        if (qraftIntegrationEnabled) {
            try {
                image = serviceMap
                        .get(QrCodeGenerationServiceType.EXTERNAL)
                        .generateQrCode(request);
            } catch (Exception exception) {
                logger.error("Some error was occurred while calling external qr code generation service. " +
                        "Generate message by internal service. Message = [{}]", exception.getMessage());
                image = serviceMap
                        .get(QrCodeGenerationServiceType.INTERNAL)
                        .generateQrCode(request);
            }
        } else {
            image = serviceMap
                    .get(QrCodeGenerationServiceType.INTERNAL)
                    .generateQrCode(request);
        }

        logger.info("Successful generate qr code");
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
