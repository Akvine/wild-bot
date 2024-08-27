package ru.akvine.wild.bot.job.sync;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.entities.CardTypeEntity;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.repositories.CardTypeRepository;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.type.CardTypeResponse;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Component
public class SyncCardTypeJob {
    private final WildberriesIntegrationService wildberriesIntegrationService;
    private final CardTypeRepository cardTypeRepository;

    public void sync() {
        logger.info("Start card types sync...");

        // TODO: вынести валидацию в отдельный прокси-сервис WildberriesIntegrationService
        CardTypeResponse response = wildberriesIntegrationService.getTypes();
        validate(response);

        List<String> wbCardTypes = response.getData();
        List<String> dbCardTypes = cardTypeRepository
                .findAll()
                .stream()
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

        logger.info("End card types sync");
    }

    private void validate(CardTypeResponse response) {
        if (Boolean.parseBoolean(response.getError())) {
            String errorMessage = String.format(
                    "Error while getting card types! Error = [%s]. Message = [%s]",
                    response.getError(),
                    response.getErrorText());
            throw new IntegrationException(errorMessage);
        }
    }
}
