package ru.akvine.wild.bot.job.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.entities.CardTypeEntity;
import ru.akvine.wild.bot.repositories.CardTypeRepository;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.domain.ClientModel;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.type.CardTypeResponse;

@RequiredArgsConstructor
@Slf4j
@Component
public class SyncCardTypeJob {
    private final WildberriesIntegrationService wildberriesIntegrationService;
    private final CardTypeRepository cardTypeRepository;
    private final ClientService clientService;
    private final ExecutorService syncCardTypeExecutor;

    /**
     * Типы карточек - общий справочник, а не данные клиента. Поэтому параллельно выполняются только запросы к
     * Wildberries (по токену каждого клиента), а сравнение с БД и сохранение - один раз на объединённом списке:
     * иначе потоки вставляли бы одни и те же новые типы одновременно.
     */
    public void sync() {
        logger.info("Start card types sync...");

        List<ClientModel> activeClients = clientService.getAllActive();
        Set<String> wbCardTypes = ConcurrentHashMap.newKeySet();
        AtomicInteger failedCount = new AtomicInteger();
        List<CompletableFuture<Void>> futures = activeClients.stream()
                .map(activeClient -> CompletableFuture.runAsync(
                                () -> {
                                    logger.info("Sync card types for client with uuid [{}]", activeClient.getUuid());
                                    CardTypeResponse response =
                                            wildberriesIntegrationService.getTypes(activeClient.getToken());
                                    wbCardTypes.addAll(response.getData());
                                },
                                syncCardTypeExecutor)
                        .exceptionally(error -> {
                            failedCount.incrementAndGet();
                            logger.error(
                                    "Card types sync failed for client with uuid [{}]", activeClient.getUuid(), error);
                            return null;
                        }))
                .toList();
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        // если WB не ответил ни у одного клиента, типы в БД не трогаем
        if (failedCount.get() < activeClients.size()) {
            saveNewTypes(wbCardTypes);
        }

        logger.info("End card types sync! Clients = {}, failed = {}", activeClients.size(), failedCount.get());
    }

    private void saveNewTypes(Set<String> wbCardTypes) {
        List<String> dbCardTypes = cardTypeRepository.findAll().stream()
                .map(CardTypeEntity::getType)
                .toList();

        List<String> uniqueCardTypesInWb = new ArrayList<>(wbCardTypes);
        uniqueCardTypesInWb.removeAll(dbCardTypes);

        List<String> uniqueCardTypesInDb = new ArrayList<>(dbCardTypes);
        uniqueCardTypesInDb.removeAll(wbCardTypes);

        if (CollectionUtils.isNotEmpty(uniqueCardTypesInWb)) {
            logger.info("Save new card types from wb = {}", uniqueCardTypesInWb);
            uniqueCardTypesInWb.forEach(uniqueType -> {
                CardTypeEntity cardTypeToSave = new CardTypeEntity().setType(uniqueType);
                cardTypeRepository.save(cardTypeToSave);
            });
        }

        if (CollectionUtils.isNotEmpty(uniqueCardTypesInDb)) {
            logger.info("Delete new card types from db = {}", uniqueCardTypesInDb);
        }
    }
}
