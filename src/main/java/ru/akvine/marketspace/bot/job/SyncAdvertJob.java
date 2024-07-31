package ru.akvine.marketspace.bot.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import ru.akvine.marketspace.bot.constants.MDCConstants;
import ru.akvine.marketspace.bot.entities.AdvertEntity;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.repositories.AdvertRepository;
import ru.akvine.marketspace.bot.services.AdvertService;
import ru.akvine.marketspace.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertDto;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertListResponse;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertsInfoResponse;
import ru.akvine.marketspace.bot.utils.MathUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class SyncAdvertJob {
    private final AdvertRepository advertRepository;
    private final AdvertService advertService;
    private final WildberriesIntegrationService wildberriesIntegrationService;
    private final String name;

    private static final int ADVERT_PAUSE_STATUS_CODE = 11;
    private static final int ADVERT_READY_FOR_START_STATUS_CODE = 4;

    @Scheduled(fixedDelayString = "${sync.advert.cron.milliseconds}")
    public void sync() {
        logger.info("Start advert sync...");
        MDC.put(MDCConstants.USERNAME, name);
        AdvertListResponse advertListResponse = wildberriesIntegrationService.getAdverts();
        if (advertListResponse.getAll() != 0) {
            List<Integer> advertsInWb = advertListResponse
                    .getAdverts()
                    .stream()
                    .filter(advertStatisticDto -> advertStatisticDto.getStatus() == ADVERT_PAUSE_STATUS_CODE || advertStatisticDto.getStatus() == ADVERT_READY_FOR_START_STATUS_CODE)
                    .flatMap(advertStatisticDto -> advertStatisticDto.getAdvertList().stream().map(AdvertDto::getAdvertId))
                    .toList();
            List<AdvertEntity> advertsInDb = advertRepository.findByStatuses(List.of(AdvertStatus.PAUSE, AdvertStatus.READY_FOR_START));
            List<Integer> advertsIdsInDb = advertsInDb
                    .stream()
                    .map(AdvertEntity::getAdvertId)
                    .collect(Collectors.toList());

            List<Integer> commonElements = new ArrayList<>(advertsInWb);
            commonElements.retainAll(advertsIdsInDb);

            List<Integer> uniqueAdvertsInWb = new ArrayList<>(advertsInWb);
            uniqueAdvertsInWb.removeAll(commonElements);

            List<Integer> uniqueAdvertsInDb = new ArrayList<>(advertsIdsInDb);
            uniqueAdvertsInDb.removeAll(commonElements);

            if (!CollectionUtils.isEmpty(uniqueAdvertsInDb)) {
                logger.info("Delete unused db adverts");
                advertsInDb
                        .stream()
                        .filter(advertEntity -> uniqueAdvertsInDb.contains(advertEntity.getAdvertId()))
                        .forEach(advertEntity -> {
                            advertEntity.setDeleted(true);
                            advertEntity.setDeletedDate(LocalDateTime.now());
                            advertRepository.save(advertEntity);
                        });
            }

            if (!CollectionUtils.isEmpty(uniqueAdvertsInWb)) {
                int batchSize = 50;
                int batchNumber = 1;
                int batchSavedCount = 0;
                for (int i = 0; i < uniqueAdvertsInWb.size(); i += batchSize) {
                    logger.info("Get info for adverts by batch with number = {} and max size = {}", batchNumber, batchSize);
                    List<Integer> advertIdsBatch = uniqueAdvertsInWb.subList(i, Math.min(i + batchSize, uniqueAdvertsInWb.size()));
                    AdvertsInfoResponse response = wildberriesIntegrationService.getAdvertsInfo(advertIdsBatch);
                    List<AdvertDto> filteredAdverts = response
                            .getAdverts()
                            .stream()
                            .filter(advertDto -> advertDto.getStatus() == ADVERT_PAUSE_STATUS_CODE
                                    || advertDto.getStatus() == ADVERT_READY_FOR_START_STATUS_CODE)
                            .filter(advertDto -> advertDto.getAdvertParams() != null
                                    && advertDto.getAdvertParams().getSubject() != null)
                            .collect(Collectors.toList());
                    advertService.saveAll(filteredAdverts);

                    batchSavedCount += filteredAdverts.size();
                    batchNumber += 1;
                }
                printSaveStatistic(batchSavedCount, uniqueAdvertsInWb.size());
            }
        }

        logger.info("End advert sync successful!");
    }

    private void printSaveStatistic(int savedCount, int totalCount) {
        logger.info(
                "Successful save {} count | Total {} count | Successful save percent {}",
                savedCount,
                totalCount,
                MathUtils.round((double) savedCount / totalCount * 100, 2)
        );
    }
}
