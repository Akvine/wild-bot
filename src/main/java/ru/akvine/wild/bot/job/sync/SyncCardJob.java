package ru.akvine.wild.bot.job.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.entities.CardEntity;
import ru.akvine.wild.bot.repositories.CardRepository;
import ru.akvine.wild.bot.services.CardService;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.domain.ClientModel;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.CardDto;

@RequiredArgsConstructor
@Slf4j
@Component
public class SyncCardJob {
    private final WildberriesIntegrationService wildberriesIntegrationService;
    private final CardRepository cardRepository;
    private final CardService cardService;
    private final ClientService clientService;
    private final ExecutorService syncCardExecutor;

    public void sync() {
        logger.info("Start card sync...");

        List<ClientModel> activeClients = clientService.getAllActive();

        // Синхронизация карточек одного клиента: клиенты не пересекаются по данным, поэтому обрабатываются параллельно
        AtomicInteger failedCount = new AtomicInteger();
        List<CompletableFuture<Void>> futures = activeClients.stream()
                .map(activeClient -> CompletableFuture.runAsync(() -> syncClient(activeClient), syncCardExecutor)
                        .exceptionally(error -> {
                            failedCount.incrementAndGet();
                            logger.error("Card sync failed for client with uuid [{}]", activeClient.getUuid(), error);
                            return null;
                        }))
                .toList();
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        logger.info("End card sync! Clients = {}, failed = {}", activeClients.size(), failedCount.get());
    }

    private void syncClient(ClientModel activeClient) {
        logger.info("Start card sync for client with uuid = [{}]", activeClient.getUuid());

        List<CardDto> cardsDto = wildberriesIntegrationService.getCards(activeClient.getToken());
        if (CollectionUtils.isEmpty(cardsDto)) {
            return;
        }

        List<CardEntity> cards = cardRepository.findAll(activeClient.getUuid());
        List<Integer> cardsIdDb = cards.stream().map(CardEntity::getExternalId).collect(Collectors.toList());
        List<Integer> cardsInWb = cardsDto.stream().map(CardDto::getNmID).toList();

        List<Integer> commonElements = new ArrayList<>(cardsInWb);
        commonElements.retainAll(cardsIdDb);

        List<Integer> uniqueCardsInWb = new ArrayList<>(cardsInWb);
        uniqueCardsInWb.removeAll(commonElements);

        List<Integer> uniqueCardsInDb = new ArrayList<>(cardsIdDb);
        uniqueCardsInDb.removeAll(commonElements);

        if (CollectionUtils.isNotEmpty(uniqueCardsInDb)) {
            logger.info(
                    "Delete unused db cards. Size = {}. Client uuid = [{}]",
                    uniqueCardsInDb.size(),
                    activeClient.getUuid());
            cards.stream()
                    .filter(cardEntity -> uniqueCardsInDb.contains(cardEntity.getExternalId()))
                    .forEach(cardEntity -> {
                        cardEntity.markDeleted();
                        cardRepository.save(cardEntity);
                    });
        }

        if (CollectionUtils.isNotEmpty(uniqueCardsInWb)) {
            logger.info(
                    "Save new cards in db. Size = {}. Client uuid = [{}]",
                    uniqueCardsInWb.size(),
                    activeClient.getUuid());
            List<CardDto> newCardsDto = cardsDto.stream()
                    .filter(cardDto -> uniqueCardsInWb.contains(cardDto.getNmID()))
                    .collect(Collectors.toList());
            cardService.create(newCardsDto, activeClient.getUuid());
        }
    }
}
