package ru.akvine.wild.bot.job.sync;

import java.util.ArrayList;
import java.util.List;
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

    public void sync() {
        logger.info("Start card types sync...");

        // TODO: можно распараллелить через CompletableFuture
        List<ClientModel> activeClients = clientService.getAllActive();
        for (ClientModel activeClient : activeClients) {
            logger.info("Sync card types for client with uuid [{}]", activeClient.getUuid());
            CardTypeResponse response = wildberriesIntegrationService.getTypes(activeClient.getToken());

            List<String> wbCardTypes = response.getData();
            List<String> dbCardTypes = cardTypeRepository.findAll().stream()
                    .map(CardTypeEntity::getType)
                    .toList();

            List<String> commonElements = new ArrayList<>(wbCardTypes);
            commonElements.retainAll(dbCardTypes);

            List<String> uniqueCardTypesInWb = new ArrayList<>(wbCardTypes);
            uniqueCardTypesInWb.removeAll(commonElements);

            List<String> uniqueCardTypesInDb = new ArrayList<>(dbCardTypes);
            uniqueCardTypesInDb.removeAll(commonElements);

            if (CollectionUtils.isNotEmpty(uniqueCardTypesInWb)) {
                logger.info("Save new card types from wb = {}", uniqueCardTypesInDb);
                uniqueCardTypesInWb.forEach(uniqueType -> {
                    CardTypeEntity cardTypeToSave = new CardTypeEntity().setType(uniqueType);
                    cardTypeRepository.save(cardTypeToSave);
                });
            }

            if (CollectionUtils.isNotEmpty(uniqueCardTypesInDb)) {
                logger.info("Delete new card types from db = {}", uniqueCardTypesInDb);
            }
        }

        logger.info("End card types sync");
    }
}
