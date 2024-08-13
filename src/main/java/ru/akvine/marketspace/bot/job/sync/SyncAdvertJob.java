package ru.akvine.marketspace.bot.job.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import ru.akvine.marketspace.bot.entities.AdvertEntity;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.repositories.AdvertRepository;
import ru.akvine.marketspace.bot.services.AdvertService;
import ru.akvine.marketspace.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertDto;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertListResponse;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertsInfoResponse;
import ru.akvine.marketspace.bot.utils.MathUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Component
public class SyncAdvertJob {
    private final AdvertRepository advertRepository;
    private final AdvertService advertService;
    private final WildberriesIntegrationService wildberriesIntegrationService;

    public void sync() {
        logger.info("Start advert sync...");

        AdvertListResponse advertListResponse = wildberriesIntegrationService.getAdverts();
        if (advertListResponse.getAll() != 0) {
            List<Integer> advertsInWb = advertListResponse
                    .getAdverts()
                    .stream()
                    .filter(advertStatisticDto -> advertStatisticDto.getStatus() == AdvertStatus.PAUSE.getCode()
                            || advertStatisticDto.getStatus() == AdvertStatus.READY_FOR_START.getCode())
                    .flatMap(advertStatisticDto -> advertStatisticDto.getAdvertList().stream().map(AdvertDto::getAdvertId))
                    .toList();
            List<AdvertEntity> advertsInDb = advertRepository.findByStatuses(List.of(AdvertStatus.PAUSE, AdvertStatus.READY_FOR_START));
            List<Integer> advertsIdsInDb = advertsInDb
                    .stream()
                    .map(AdvertEntity::getExternalId)
                    .collect(Collectors.toList());

            List<Integer> commonElements = new ArrayList<>(advertsInWb);
            commonElements.retainAll(advertsIdsInDb);

            List<Integer> uniqueAdvertsInWb = new ArrayList<>(advertsInWb);
            uniqueAdvertsInWb.removeAll(commonElements);

            List<Integer> uniqueAdvertsInDb = new ArrayList<>(advertsIdsInDb);
            uniqueAdvertsInDb.removeAll(commonElements);

            if (CollectionUtils.isNotEmpty(uniqueAdvertsInDb)) {
                logger.info("Delete unused db adverts");
                advertsInDb
                        .stream()
                        .filter(advertEntity -> uniqueAdvertsInDb.contains(advertEntity.getExternalId()))
                        .forEach(advertEntity -> {
                            advertEntity.setDeleted(true);
                            advertEntity.setDeletedDate(ZonedDateTime.now());
                            advertRepository.save(advertEntity);
                        });
            }

            if (CollectionUtils.isNotEmpty(uniqueAdvertsInWb)) {
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
                            .filter(advertDto -> advertDto.getStatus() == AdvertStatus.PAUSE.getCode()
                                    || advertDto.getStatus() == AdvertStatus.READY_FOR_START.getCode())
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
