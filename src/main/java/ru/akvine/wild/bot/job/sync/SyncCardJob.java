package ru.akvine.wild.bot.job.sync;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    public void sync() {
        logger.info("Start card sync...");

        // TODO: можно распараллелить через CompletableFuture
        List<ClientModel> activeClients = clientService.getAllActive();
        for (ClientModel activeClient : activeClients) {
            logger.info("Start card sync for client with uuid = [{}]", activeClient.getUuid());

            List<CardDto> cardsDto = wildberriesIntegrationService.getCards(activeClient.getToken());
            if (CollectionUtils.isNotEmpty(cardsDto)) {
                List<CardEntity> cards = cardRepository.findAll();
                List<Integer> cardsIdDb =
                        cards.stream().map(CardEntity::getExternalId).collect(Collectors.toList());
                List<Integer> cardsInWb = cardsDto.stream().map(CardDto::getNmID).toList();

                List<Integer> commonElements = new ArrayList<>(cardsInWb);
                commonElements.retainAll(cardsIdDb);

                List<Integer> uniqueCardsInWb = new ArrayList<>(cardsInWb);
                uniqueCardsInWb.removeAll(commonElements);

                List<Integer> uniqueCardsInDb = new ArrayList<>(cardsIdDb);
                uniqueCardsInDb.removeAll(commonElements);

                if (CollectionUtils.isNotEmpty(uniqueCardsInDb)) {
                    logger.info("Delete unused db cards. Size = {}. Client uuid = [{}]", uniqueCardsInDb.size(), activeClient.getUuid());
                    cards.stream()
                            .filter(cardEntity -> uniqueCardsInDb.contains(cardEntity.getExternalId()))
                            .forEach(cardEntity -> {
                                cardEntity.setDeleted(true);
                                cardEntity.setDeletedDate(LocalDateTime.now());
                                cardRepository.save(cardEntity);
                            });
                }

                if (CollectionUtils.isNotEmpty(uniqueCardsInWb)) {
                    logger.info("Save new cards in db. Size = {}. Client uuid = [{}]", uniqueCardsInWb.size(), activeClient.getUuid());
                    List<CardDto> newCardsDto = cardsDto.stream()
                            .filter(cardDto -> uniqueCardsInWb.contains(cardDto.getNmID()))
                            .collect(Collectors.toList());
                    cardService.create(newCardsDto);
                }
            }
        }

        logger.info("End card sync...");
    }
}
