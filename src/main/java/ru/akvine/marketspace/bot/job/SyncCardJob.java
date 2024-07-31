package ru.akvine.marketspace.bot.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import ru.akvine.marketspace.bot.entities.CardEntity;
import ru.akvine.marketspace.bot.repositories.CardRepository;
import ru.akvine.marketspace.bot.services.CardService;
import ru.akvine.marketspace.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.CardDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class SyncCardJob {
    private final WildberriesIntegrationService wildberriesIntegrationService;
    private final CardRepository cardRepository;
    private final CardService cardService;
    private final String name;

    @Scheduled(fixedDelayString = "${sync.card.cron.milliseconds}")
    public void sync() {
        logger.info("Start card sync...");
        MDC.put("username", name);
        List<CardDto> cardsDto = wildberriesIntegrationService.getCards();
        if (!CollectionUtils.isEmpty(cardsDto)) {
            List<CardEntity> cards = cardRepository.findAll();
            List<Integer> cardsIdDb = cards
                    .stream()
                    .map(CardEntity::getItemId)
                    .collect(Collectors.toList());
            List<Integer> cardsInWb = cardsDto
                    .stream()
                    .map(CardDto::getNmID)
                    .toList();

            List<Integer> commonElements = new ArrayList<>(cardsInWb);
            commonElements.retainAll(cardsIdDb);

            List<Integer> uniqueCardsInWb = new ArrayList<>(cardsInWb);
            uniqueCardsInWb.removeAll(commonElements);

            List<Integer> uniqueCardsInDb = new ArrayList<>(cardsIdDb);
            uniqueCardsInDb.removeAll(commonElements);

            if (!CollectionUtils.isEmpty(uniqueCardsInDb)) {
                logger.info("Delete unused db cards. Size = {}", uniqueCardsInDb.size());
                cards
                        .stream()
                        .filter(cardEntity -> uniqueCardsInDb.contains(cardEntity.getItemId()))
                        .forEach(cardEntity -> {
                            cardEntity.setDeleted(true);
                            cardEntity.setDeletedDate(LocalDateTime.now());
                            cardRepository.save(cardEntity);
                        });
            }

            if (!CollectionUtils.isEmpty(uniqueCardsInWb)) {
                logger.info("Save new cards in db. Size = {}", uniqueCardsInWb.size());
                List<CardDto> newCardsDto = cardsDto
                        .stream()
                        .filter(cardDto -> uniqueCardsInWb.contains(cardDto.getNmID()))
                        .collect(Collectors.toList());
                cardService.create(newCardsDto);
            }
        }
        logger.info("End card sync...");
    }
}
