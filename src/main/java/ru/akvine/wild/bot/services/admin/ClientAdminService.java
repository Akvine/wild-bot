package ru.akvine.wild.bot.services.admin;

import com.google.common.base.Preconditions;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.akvine.wild.bot.entities.ClientBlockedCredentialsEntity;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.exceptions.ClientNotFoundException;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.repositories.ClientRepository;
import ru.akvine.wild.bot.repositories.specifications.ClientSpecification;
import ru.akvine.wild.bot.services.ClientBlockingService;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.domain.ClientModel;
import ru.akvine.wild.bot.services.dto.admin.client.*;
import ru.akvine.wild.bot.services.integration.BotIntegrationAdapter;
import ru.akvine.wild.bot.utils.DateUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientAdminService {
    private final ClientBlockingService clientBlockingService;
    private final ClientService clientService;
    // TODO : лучше делать обновление сущности в ClientService, так по канону
    private final ClientRepository clientRepository;
    private final BotIntegrationAdapter botIntegrationAdapter;
    private final ClientSpecification clientSpecification;
    private final ExecutorService messageExecutor;

    public List<ClientModel> list(ListClients listClients) {

        Specification<ClientEntity> specification = clientSpecification.build(listClients);
        Pageable pageable = PageRequest.of(listClients.getPage(), listClients.getCount());
        return clientRepository.findAll(specification, pageable).map(ClientModel::new).stream()
                .toList();
    }

    public ClientModel addTestsToClient(AddTests addTests) {
        ClientEntity client;
        if (StringUtils.isNotBlank(addTests.getUsername())) {
            client = clientService.verifyExistsByUsername(addTests.getUsername());
        } else {
            client = clientService.verifyExistsByChatIdAndBotType(addTests.getChatId(), addTests.getBotType());
        }

        client.increaseAvailableTestsCount(addTests.getTestsCount());
        return new ClientModel(clientRepository.save(client));
    }

    public BlockClientFinish blockClient(BlockClientStart start) {
        Preconditions.checkNotNull(start, "blockClientStart is null");
        long minutes = start.getMinutes();
        String chatId;
        BotType botType = start.getBotType();

        if (StringUtils.isNotBlank(start.getUuid())) {
            chatId = clientService.verifyExistsByClientUuid(start.getUuid()).getChatId();
        } else {
            chatId = clientService
                    .verifyExistsByChatIdAndBotType(start.getChatId(), botType)
                    .getChatId();
        }

        LocalDateTime blockDate = LocalDateTime.now().plusMinutes(minutes);
        clientBlockingService.setBlock(chatId, botType, minutes);

        return new BlockClientFinish().setChatId(chatId).setDateTime(blockDate).setMinutes(minutes);
    }

    public List<BlockClientEntry> listBlocked() {
        List<ClientBlockedCredentialsEntity> list = clientBlockingService.list();

        return list.stream()
                .map(obj -> {
                    LocalDateTime start = obj.getBlockStartDate();
                    LocalDateTime end = obj.getBlockEndDate();
                    String chatId = obj.getChatId();
                    long minutes = DateUtils.getMinutes(start, end);
                    return new BlockClientEntry()
                            .setChatId(chatId)
                            .setBlockStartDate(start)
                            .setBlockEndDate(end)
                            .setMinutes(minutes);
                })
                .collect(Collectors.toList());
    }

    public void unblockClient(UnblockClient unblockClient) {
        Preconditions.checkNotNull(unblockClient, "unblockClient is null");
        String chatId;
        BotType botType = unblockClient.getBotType();

        if (StringUtils.isNotBlank(unblockClient.getUuid())) {
            chatId = clientService
                    .verifyExistsByClientUuid(unblockClient.getUuid())
                    .getChatId();
        } else if (StringUtils.isNotBlank(unblockClient.getChatId()) && unblockClient.getBotType() != null) {
            chatId = clientService
                    .verifyExistsByChatIdAndBotType(unblockClient.getChatId(), unblockClient.getBotType())
                    .getChatId();
        } else {
            chatId = clientService
                    .verifyExistsByUsername(unblockClient.getUsername())
                    .getChatId();
        }

        clientBlockingService.removeBlock(chatId, botType);
    }

    public void sendMessage(SendMessage sendMessage) {
        Preconditions.checkNotNull(sendMessage, "sendMessage is null");
        logger.info("Send message by request = {}", sendMessage);

        String message = sendMessage.getMessage();
        BotType botType = sendMessage.getBotType();
        List<ClientModel> activeClients;
        if (!CollectionUtils.isEmpty(sendMessage.getChatIds())) {
            activeClients = clientService.getByListChatId(sendMessage.getChatIds());
            if (CollectionUtils.isEmpty(activeClients)) {
                String errorMessage =
                        String.format("Not found any client with chat ids = %s", sendMessage.getChatIds());
                throw new ClientNotFoundException(errorMessage);
            }

            List<String> failedChatIds = new ArrayList<>();
            int total = sendMessageInternal(activeClients, botType, message, failedChatIds);
            failIfNotDelivered(total, failedChatIds);
            return;
        }

        int total = 0;
        List<String> failedChatIds = new ArrayList<>();
        long lastId = 0;
        while (true) {
            List<ClientModel> batch = clientService.getBatchAfterId(lastId);
            if (batch.isEmpty()) {
                break;
            }

            List<ClientModel> botClients = batch.stream()
                    .filter(client -> client.getBotType() == botType)
                    .toList();
            if (!botClients.isEmpty()) {
                total += sendMessageInternal(botClients, botType, message, failedChatIds);
            }
            lastId = batch.getLast().getId();
        }
        failIfNotDelivered(total, failedChatIds);
    }

    public void addToWhitelist(Whitelist whitelist) {
        Preconditions.checkNotNull(whitelist, "whitelist is null");
        logger.info("Add client to whitelist by {}", whitelist);

        ClientEntity client;
        if (StringUtils.isNotBlank(whitelist.getChatId())) {
            client = clientService.verifyExistsByChatIdAndBotType(whitelist.getChatId(), whitelist.getBotType());
        } else {
            client = clientService.verifyExistsByUsername(whitelist.getUsername());
        }

        client.setInWhitelist(true);
        clientRepository.save(client);
        logger.info(
                "Successful add to whitelist client with chatId = {} and username = {}",
                client.getChatId(),
                client.getUsername());
    }

    public void deleteFromWhitelist(Whitelist whitelist) {
        Preconditions.checkNotNull(whitelist, "whitelist is null");
        logger.info("Delete client from whitelist by {}", whitelist);

        ClientEntity client;
        if (StringUtils.isNotBlank(whitelist.getChatId())) {
            client = clientService.verifyExistsByChatIdAndBotType(whitelist.getChatId(), whitelist.getBotType());
        } else {
            client = clientService.verifyExistsByUsername(whitelist.getUsername());
        }

        client.setInWhitelist(false);
        clientRepository.save(client);
        logger.info(
                "Successful delete to whitelist client with chatId = {} and username = {}",
                client.getChatId(),
                client.getUsername());
    }

    /**
     * Отправляет сообщение каждому клиенту отдельным запросом параллельно: недоступный клиент (заблокировал бота,
     * удалил чат) не срывает рассылку остальным, как это было при отправке всей пачки одним вызовом.
     *
     * @param failedChatIds в этот список добавляются чаты, в которые доставить не удалось
     * @return сколько чатов обработано
     */
    private int sendMessageInternal(
            List<ClientModel> activeClients, BotType botType, String message, List<String> failedChatIds) {
        Set<String> activeChatIds =
                activeClients.stream().map(ClientModel::getChatId).collect(Collectors.toSet());
        List<CompletableFuture<String>> futures = activeChatIds.stream()
                .map(chatId -> CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                botIntegrationAdapter.sendMessage(Set.of(chatId), botType, message);
                                return null;
                            } catch (Exception exception) {
                                logger.warn(
                                        "Can't send message to chat with id = [{}]: {}",
                                        chatId,
                                        exception.getMessage());
                                return chatId;
                            }
                        },
                        messageExecutor))
                .toList();
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        futures.stream().map(CompletableFuture::join).filter(Objects::nonNull).forEach(failedChatIds::add);
        return activeChatIds.size();
    }

    /**
     * Рассылка доведена до всех доступных клиентов; если кому-то не доставлено, администратор узнаёт об этом
     * ошибкой в конце, а не обрывом рассылки на первом сбое
     */
    private void failIfNotDelivered(int total, List<String> failedChatIds) {
        if (failedChatIds.isEmpty()) {
            return;
        }
        String errorMessage = String.format(
                "Message was not delivered to %d of %d clients. First failed chat ids = %s",
                failedChatIds.size(), total, failedChatIds.stream().limit(20).toList());
        throw new IntegrationException(errorMessage);
    }
}
