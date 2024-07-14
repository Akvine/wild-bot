package ru.akvine.marketspace.bot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.entities.AdvertEntity;
import ru.akvine.marketspace.bot.entities.AdvertStatisticEntity;
import ru.akvine.marketspace.bot.repositories.AdvertStatisticRepository;
import ru.akvine.marketspace.bot.services.domain.AdvertStatisticBean;
import ru.akvine.marketspace.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertFullStatisticDatesDto;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertFullStatisticIntervalDto;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertFullStatisticResponse;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertStatisticInterval;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertStatisticService {
    private final WildberriesIntegrationService wildberriesIntegrationService;
    private final AdvertStatisticRepository advertStatisticRepository;

    public AdvertStatisticBean getAndSave(AdvertEntity advert) {
        logger.info("Start getting advert full statistic for advert = [{}]", advert);

        AdvertFullStatisticResponse[] response;
        Duration duration = Duration.between(advert.getStartCheckDateTime(), LocalDateTime.now());
        if (duration.toHours() < 24) {
            logger.info("Get advert with id = {} statistic by dates request", advert.getAdvertId());
            List<AdvertFullStatisticDatesDto> request = List.of(
                    new AdvertFullStatisticDatesDto()
                            .setId(advert.getAdvertId())
                            .setDates(List.of(LocalDate.now().toString()))
            );
            response = wildberriesIntegrationService.getAdvertsFullStatisticByDates(request);
        } else {
            logger.info("Get advert with id = {} statistic by interval request", advert.getAdvertId());
            List<AdvertFullStatisticIntervalDto> request = List.of(
                    new AdvertFullStatisticIntervalDto()
                            .setId(advert.getAdvertId())
                            .setInterval(new AdvertStatisticInterval()
                                    .setBegin(advert.getStartCheckDateTime().toLocalDate().toString())
                                    .setEnd(LocalDate.now().toString()))
            );
            response = wildberriesIntegrationService.getAdvertsFullStatisticByInterval(request);
        }

        AdvertFullStatisticResponse firstPositionResponse = response[0];
        AdvertStatisticEntity advertStatisticEntity = new AdvertStatisticEntity()
                .setViews(firstPositionResponse.getViews())
                .setClicks(firstPositionResponse.getClicks())
                .setCtr(firstPositionResponse.getCtr())
                .setCpc(firstPositionResponse.getCpc())
                .setSum(firstPositionResponse.getSum())
                .setAtbs(firstPositionResponse.getAtbs())
                .setOrders(firstPositionResponse.getOrders())
                .setCr(firstPositionResponse.getCr())
                .setShks(firstPositionResponse.getShks())
                .setSumPrice(firstPositionResponse.getSumPrice())
                .setAdvertEntity(advert)
                .setClient(advert.getClient());

        AdvertStatisticBean savedAdvertStatistic = new AdvertStatisticBean(advertStatisticRepository.save(advertStatisticEntity));
        logger.info("Successful get statistic from wb and save it = [{}]", savedAdvertStatistic);
        return savedAdvertStatistic;
    }
}
