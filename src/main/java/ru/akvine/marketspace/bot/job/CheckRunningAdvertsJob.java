package ru.akvine.marketspace.bot.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import ru.akvine.marketspace.bot.constants.MDCConstants;
import ru.akvine.marketspace.bot.entities.AdvertEntity;
import ru.akvine.marketspace.bot.entities.CardEntity;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.repositories.AdvertRepository;
import ru.akvine.marketspace.bot.services.AdvertStatisticService;
import ru.akvine.marketspace.bot.services.CardService;
import ru.akvine.marketspace.bot.infrastructure.counter.CountersStorage;
import ru.akvine.marketspace.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.marketspace.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertChangeCpmRequest;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.ChangeStocksRequest;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.SkuDto;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class CheckRunningAdvertsJob {
    private final AdvertRepository advertRepository;
    private final CardService cardService;
    private final TelegramIntegrationService telegramIntegrationService;
    private final WildberriesIntegrationService wildberriesIntegrationService;
    private final CountersStorage countersStorage;
    private final AdvertStatisticService advertStatisticService;

    private final String name;
    private final String chatId;

    @Value("${check.advert.cron.milliseconds}")
    private long checkMilliseconds;
    @Value("${max.start.sum.difference}")
    private int maxStartSumDifference;
    @Value("${advert.cpm.increase.value}")
    private int advertCpmIncreaseValue;
    @Value("${check.advert.iterations.before.increase}")
    private int maxIterationsBeforeIncreaseCpm;
    @Value("${advert.max.cpm}")
    private int advertMaxCpm;

    @Scheduled(fixedDelayString = "${check.advert.cron.milliseconds}")
    public void checkRunningAdverts() {
        MDC.put(MDCConstants.USERNAME, name);
        MDC.put(MDCConstants.CHAT_ID, chatId);
        logger.info("Start check running adverts...");

        List<AdvertEntity> runningAdverts = advertRepository.findByStatuses(List.of(AdvertStatus.RUNNING));
        LocalDateTime startCheckDateTime = LocalDateTime.now();
        for (AdvertEntity advert : runningAdverts) {
            int advertId = advert.getExternalId();
            int currentBudgetSum = wildberriesIntegrationService.getAdvertBudgetInfo(advertId).getTotal();
            int startBudgetSum = advert.getStartBudgetSum();
            int differenceBudgetSum = startBudgetSum - currentBudgetSum;
            int currentCpm = advert.getCpm();

            if (currentBudgetSum == 0 || differenceBudgetSum >= maxStartSumDifference) {
                logger.info("Get statistic and pause advert with id = {}", advertId);
                advertStatisticService.getAndSave(advert);
                if (currentBudgetSum != 0) {
                    logger.info("Current budget for advert = [{}] not equals zero, pause advert", advert);
                    wildberriesIntegrationService.pauseAdvert(advertId);
                }
                CardEntity cardEntity = advert.getCard();
                ChangeStocksRequest request = new ChangeStocksRequest()
                        .setStocks(List.of(new SkuDto()
                                .setAmount(0)
                                .setSku(cardEntity.getBarcode())));
                wildberriesIntegrationService.changeStocks(request);

                String chatId = advert.getClient().getChatId();
                advert.setStatus(AdvertStatus.PAUSE);
                advert.setOrdinalStatus(AdvertStatus.PAUSE.getCode());
                advert.setUpdatedDate(LocalDateTime.now());
                advert.setNextCheckDateTime(null);
                advert.setCheckBudgetSum(null);
                advert.setClient(null);
                advert.setLocked(false);
                advertRepository.save(advert);
                countersStorage.delete(advertId);

                String finishedTestMessage = String.format(
                        "Тест с advert id = %s успешно завершился.\nВведите команду /report для просмотра отчета",
                        advertId
                );
                telegramIntegrationService.sendMessage(chatId, finishedTestMessage);
                continue;
            }
            if (currentCpm < advertMaxCpm) {
                if (countersStorage.check(advertId, maxIterationsBeforeIncreaseCpm)) {
                    logger.info("Increase cpm for advert with id = {}", advertId);

                    int newCpm = advert.getCpm() + advertCpmIncreaseValue;
                    AdvertChangeCpmRequest request = new AdvertChangeCpmRequest()
                            .setCpm(newCpm)
                            .setAdvertId(advertId)
                            .setParam(advert.getCard().getCategoryId())
                            .setType(advert.getOrdinalType());
                    wildberriesIntegrationService.changeAdvertCpm(request);

                    advert.setCpm(newCpm);
                }
                countersStorage.increase(advertId);
            }
            long seconds = checkMilliseconds / 1000;
            advert.setCheckBudgetSum(currentBudgetSum);
            advert.setNextCheckDateTime(startCheckDateTime.plusSeconds(seconds));
            advert.setUpdatedDate(LocalDateTime.now());
            advertRepository.save(advert);
        }
        logger.info("Successful end check running adverts...");
    }
}
