package ru.akvine.marketspace.bot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.akvine.marketspace.bot.entities.AdvertEntity;
import ru.akvine.marketspace.bot.entities.AdvertStatisticEntity;
import ru.akvine.marketspace.bot.entities.ClientEntity;
import ru.akvine.marketspace.bot.exceptions.AdvertStatisticNotFoundException;
import ru.akvine.marketspace.bot.repositories.AdvertStatisticRepository;
import ru.akvine.marketspace.bot.repositories.ClientRepository;
import ru.akvine.marketspace.bot.services.domain.AdvertModel;
import ru.akvine.marketspace.bot.services.domain.AdvertStatisticModel;
import ru.akvine.marketspace.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertFullStatisticDatesDto;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertFullStatisticIntervalDto;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertFullStatisticResponse;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertStatisticInterval;
import ru.akvine.marketspace.bot.utils.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertStatisticService {
    private final WildberriesIntegrationService wildberriesIntegrationService;
    private final AdvertStatisticRepository advertStatisticRepository;
    // TODO : сделать все через clientService
    private final ClientRepository clientRepository;
    private final ClientService clientService;
    private final AdvertService advertService;

    public AdvertStatisticModel getAndSave(AdvertEntity advert) {
        logger.info("Start getting advert full statistic for advert = [{}]", advert);

        AdvertStatisticEntity advertStatisticEntity = verifyExistsByClientIdAndAdvertId(advert.getClient().getId(), advert.getId());

        AdvertFullStatisticResponse[] response;
        if (DateUtils.isSameDay(advert.getStartCheckDateTime(), LocalDateTime.now())) {
            logger.info("Get advert with id = {} statistic by dates request", advert.getExternalId());
            List<AdvertFullStatisticDatesDto> request = List.of(
                    new AdvertFullStatisticDatesDto()
                            .setId(advert.getExternalId())
                            .setDates(List.of(LocalDate.now().toString()))
            );
            response = wildberriesIntegrationService.getAdvertsFullStatisticByDates(request);
        } else {
            logger.info("Get advert with id = {} statistic by interval request", advert.getExternalId());
            List<AdvertFullStatisticIntervalDto> request = List.of(
                    new AdvertFullStatisticIntervalDto()
                            .setId(advert.getExternalId())
                            .setInterval(new AdvertStatisticInterval()
                                    .setBegin(advert.getStartCheckDateTime().toLocalDate().toString())
                                    .setEnd(LocalDate.now().toString()))
            );
            response = wildberriesIntegrationService.getAdvertsFullStatisticByInterval(request);
        }

        AdvertFullStatisticResponse firstPositionResponse = response[0];
        advertStatisticEntity
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
                .setClient(advert.getClient())
                .setActive(false);

        AdvertStatisticModel savedAdvertStatistic = new AdvertStatisticModel(advertStatisticRepository.save(advertStatisticEntity));

        ClientEntity client = clientService.verifyExistsByChatId(advert.getClient().getChatId());
        client.decreaseOneTest();
        clientRepository.save(client);

        advert.setAvailableForStart(DateUtils.getStartOfNextDay());
        advertService.update(new AdvertModel(advert));

        logger.info("Successful get statistic from wb and save it = [{}]", savedAdvertStatistic);
        return savedAdvertStatistic;
    }

    public AdvertStatisticEntity verifyExistsByClientIdAndAdvertId(long clientId, long advertId) {
        return advertStatisticRepository
                .findByClientIdAndAdvertId(clientId, advertId)
                .orElseThrow(() -> {
                    String errorMessage = String.format(
                            "Advert statistic for client with id = [%s] and advert with id = [%s] not found!",
                            clientId, advertId);
                    return new AdvertStatisticNotFoundException(errorMessage);
                });
    }

    public AdvertStatisticEntity verifyExistsByClientIdAndId(long clientId, long id) {
        return advertStatisticRepository
                .findByClientIdAndId(clientId, id)
                .orElseThrow(() -> {
                    String errorMessage = String.format(
                            "Advert statistic for client with id = [%s] and  id = [%s] not found!",
                            clientId, id);
                    return new AdvertStatisticNotFoundException(errorMessage);
                });
    }

    @Transactional
    public void delete(AdvertStatisticEntity advertStatisticEntity) {
        advertStatisticRepository.delete(advertStatisticEntity);
    }
}
