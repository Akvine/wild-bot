package ru.akvine.wild.bot.job.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import ru.akvine.wild.bot.constants.MDCConstants;
import ru.akvine.wild.bot.entities.AdvertEntity;
import ru.akvine.wild.bot.repositories.AdvertRepository;
import ru.akvine.wild.bot.repositories.AdvertStatisticRepository;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class DeleteAdvertsAndStatisticsJob {

    private final AdvertStatisticRepository statisticRepository;
    private final AdvertRepository advertRepository;

    private final String name;
    private final String chatId;
    private final String botType;

    @Value("${advert.and.statistic.delete.job.after.days.expired}")
    private int afterDaysExpiredCount;

    @Scheduled(fixedDelayString = "${advert.and.statistic.delete.job.fixedDelay.milliseconds}")
    public void delete() {
        try {
            MDC.put(MDCConstants.USERNAME, name);
            MDC.put(MDCConstants.CHAT_ID, chatId);
            MDC.put(MDCConstants.BOT_TYPE, botType);

            logger.info("Start delete expired deleted adverts and statistics...");

            LocalDateTime thresholdDate = LocalDateTime.now().minusDays(afterDaysExpiredCount);
            List<Long> advertsIdsToDelete = advertRepository.findDeletedAfterExpiringDateCome(thresholdDate)
                    .stream().map(AdvertEntity::getId).toList();

            if (!advertsIdsToDelete.isEmpty()) {
                statisticRepository.deleteByAdvertIds(advertsIdsToDelete);
                advertRepository.deleteAll(advertsIdsToDelete);
                logger.info("Successful delete [{}] adverts count and their statistics", advertsIdsToDelete.size());
            }
        } finally {
            MDC.clear();
        }
    }
}
