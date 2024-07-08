package ru.akvine.marketspace.bot.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import ru.akvine.marketspace.bot.entities.AdvertEntity;
import ru.akvine.marketspace.bot.entities.CardEntity;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.repositories.AdvertRepository;
import ru.akvine.marketspace.bot.services.AdvertStatisticService;
import ru.akvine.marketspace.bot.services.CardService;
import ru.akvine.marketspace.bot.services.IterationsCounterService;
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
    private final IterationsCounterService iterationsCounterService;
    private final AdvertStatisticService advertStatisticService;

    @Value("${check.advert.cron.milliseconds}")
    private long checkMilliseconds;
    @Value("${max.start.sum.difference}")
    private int maxStartSumDifference;
    @Value("${default.increase.cpm.sum}")
    private int defaultIncreaseCpmSum;
    @Value("${check.advert.iterations.before.increase}")
    private int maxIterationsBeforeIncreaseCpm;

    private final static int PAUSE_STATUS_ADVERT_CODE = 11;

    @Scheduled(fixedDelayString = "${check.advert.cron.milliseconds}")
    public void checkRunningAdverts() {
        logger.info("Start check running adverts...");
        List<AdvertEntity> runningAdverts = advertRepository.findByStatuses(List.of(AdvertStatus.RUNNING));
        LocalDateTime startCheckDateTime = LocalDateTime.now();
        for (AdvertEntity advert : runningAdverts) {
            String advertId = advert.getAdvertId();
            int currentBudgetSum = wildberriesIntegrationService.getAdvertBudgetInfo(advertId).getTotal();
            int startBudgetSum = advert.getStartBudgetSum();
            int differenceBudgetSum = startBudgetSum - currentBudgetSum;

            if (currentBudgetSum == 0 || differenceBudgetSum >= maxStartSumDifference) {
                logger.info("Get statistic and pause advert with id = {}", advertId);
                advertStatisticService.getAndSave(advert);
                if (currentBudgetSum != 0) {
                    wildberriesIntegrationService.pauseAdvert(advertId);
                }
                CardEntity cardEntity = cardService.verifyExistsByItemId(advert.getItemId());
                ChangeStocksRequest request = new ChangeStocksRequest()
                        .setStocks(List.of(new SkuDto()
                                .setAmount(0)
                                .setSku(cardEntity.getBarcode())));
                wildberriesIntegrationService.changeStocks(request);

                advert.setStatus(AdvertStatus.getByCode(PAUSE_STATUS_ADVERT_CODE));
                advert.setOrdinalStatus(PAUSE_STATUS_ADVERT_CODE);
                advert.setUpdatedDate(LocalDateTime.now());
                advert.setNextCheckDateTime(null);
                advert.setCheckBudgetSum(null);
                advertRepository.save(advert);
                iterationsCounterService.delete(advertId);

                String finishedTestMessage = String.format(
                        "Тест с advert id = %s успешно завершился.\nВведите команду /report для просмотра отчета",
                        advertId
                );
                telegramIntegrationService.sendMessage(advert.getClient().getChatId(), finishedTestMessage);
                continue;
            }
            long seconds = checkMilliseconds / 1000;
            if (iterationsCounterService.check(advertId, maxIterationsBeforeIncreaseCpm)) {
                logger.info("Increase cpm for advert with id = {}", advertId);

                int newCpm = advert.getCpm() + defaultIncreaseCpmSum;
                AdvertChangeCpmRequest request = new AdvertChangeCpmRequest()
                        .setCpm(newCpm)
                        .setAdvertId(Integer.parseInt(advertId))
                        .setParam(Integer.parseInt(advert.getCategoryId()))
                        .setType(advert.getOrdinalType());
                wildberriesIntegrationService.changeAdvertCpm(request);

                advert.setCpm(newCpm);
            }
            iterationsCounterService.increase(advertId);
            advert.setCheckBudgetSum(currentBudgetSum);
            advert.setNextCheckDateTime(startCheckDateTime.plusSeconds(seconds));
            advert.setUpdatedDate(LocalDateTime.now());
            advertRepository.save(advert);
        }
        logger.info("Successful end check running adverts...");
    }
}
