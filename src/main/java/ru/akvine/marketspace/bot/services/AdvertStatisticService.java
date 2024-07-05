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
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertFullStatisticResponse;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertStatisticService {
    private final WildberriesIntegrationService wildberriesIntegrationService;
    private final AdvertStatisticRepository advertStatisticRepository;

    public AdvertStatisticBean getAndSave(AdvertEntity advert) {
        logger.info("Start getting advert full statistic for advert with id = {}", advert.getAdvertId());
        List<AdvertFullStatisticDatesDto> advertFullStatisticRequest = List.of(
                new AdvertFullStatisticDatesDto()
                        .setId(Integer.parseInt(advert.getAdvertId()))
                        .setDates(List.of(LocalDate.now().toString()))
        );
        AdvertFullStatisticResponse[] response = wildberriesIntegrationService.getAdvertsFullStatistic(advertFullStatisticRequest);
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
                .setAdvertEntity(advert);

        AdvertStatisticBean savedAdvertStatistic = new AdvertStatisticBean(advertStatisticRepository.save(advertStatisticEntity));
        logger.info("Successful get statistic from wb and save it = [{}]", savedAdvertStatistic);
        return savedAdvertStatistic;
    }
}
