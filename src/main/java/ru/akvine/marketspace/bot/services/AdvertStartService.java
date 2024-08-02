package ru.akvine.marketspace.bot.services;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.entities.AdvertEntity;
import ru.akvine.marketspace.bot.entities.AdvertStatisticEntity;
import ru.akvine.marketspace.bot.entities.CardEntity;
import ru.akvine.marketspace.bot.entities.ClientEntity;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.exceptions.AdvertStartException;
import ru.akvine.marketspace.bot.infrastructure.SessionStorage;
import ru.akvine.marketspace.bot.infrastructure.impl.ClientSessionData;
import ru.akvine.marketspace.bot.repositories.AdvertStatisticRepository;
import ru.akvine.marketspace.bot.services.counter.IterationsCounterService;
import ru.akvine.marketspace.bot.services.domain.AdvertBean;
import ru.akvine.marketspace.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.*;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.ChangeStocksRequest;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.SkuDto;
import ru.akvine.marketspace.bot.utils.DateUtils;
import ru.akvine.marketspace.bot.utils.ThreadUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertStartService {
    private final AdvertService advertService;
    private final CardService cardService;
    private final ClientService clientService;
    private final AdvertStatisticRepository advertStatisticRepository;
    private final WildberriesIntegrationService wildberriesIntegrationService;
    private final IterationsCounterService iterationsCounterService;
    private final SessionStorage<String, ClientSessionData> sessionStorage;

    @Value("${check.advert.cron.milliseconds}")
    private long checkMilliseconds;

    @Value("${advert.budget.sum.increase.value}")
    private int advertBudgetSumIncreaseValue;
    @Value("${advert.min.cpm}")
    private int advertMinCpm;
    @Value("${wildberries.change.stocks.count}")
    private int changeStocksCount;
    @Value("${advert.budget.min.sum}")
    private int budgetMinSum;

    private final static int CARD_MAIN_PHOTO_POSITION = 1;

    public AdvertBean start(String chatId) {
        // TODO : убрать локирование РК, когда будем создавать РК, если нет свободных
        try {
            Preconditions.checkNotNull(chatId, "chatId is null");
            Integer categoryId = sessionStorage.get(chatId).getChoosenCategoryId();
            logger.info("Try to start first one advert with category id = {}", categoryId);
            return startInternal(chatId);
        } catch (Exception exception) {
            AdvertBean advertBean = advertService.getByAdvertId(sessionStorage.get(chatId).getLockedAdvertId());
            advertBean.setLocked(false);
            advertService.update(advertBean);
            throw new AdvertStartException(exception.getMessage());
        }
    }

    private AdvertBean startInternal(String chatId) {
        int advertId = sessionStorage.get(chatId).getLockedAdvertId();
        AdvertBean advertToStart = advertService.getByAdvertId(advertId);
        CardEntity card = cardService.verifyExistsByItemId(advertToStart.getItemId());
        ClientEntity client = clientService.verifyExistsByChatId(chatId);

        AdvertBudgetInfoResponse advertBudgetInfo = wildberriesIntegrationService.getAdvertBudgetInfo(advertId);
        Integer advertTotalBudget = advertBudgetInfo.getTotal();

        if (advertTotalBudget < budgetMinSum) {
            wildberriesIntegrationService.advertBudgetDeposit(advertId, advertBudgetSumIncreaseValue);
            advertToStart.plusStartBudget(advertBudgetSumIncreaseValue);
        } else {
            advertToStart.setStartBudgetSum(advertTotalBudget);
        }
        advertToStart.setCheckBudgetSum(advertToStart.getStartBudgetSum());

        // TODO : убрать в рамках рефакторинга
        ThreadUtils.sleep(5000);

        AdvertChangeCpmRequest request = new AdvertChangeCpmRequest()
                .setAdvertId(advertId)
                .setType(advertToStart.getType().getCode())
                .setParam(advertToStart.getCategoryId())
                .setCpm(advertMinCpm);
        advertToStart.setCpm(advertMinCpm);
        wildberriesIntegrationService.changeAdvertCpm(request);

        String advertStartName = "Bot:" + DateUtils.formatLocalDateTime(LocalDateTime.now()) + ":" + advertId;
        wildberriesIntegrationService.renameAdvert(advertId, advertStartName);

        if (sessionStorage.get(chatId).isInputNewCardPriceAndDiscount()) {
            SetGoodPriceRequest setGoodPriceRequest = new SetGoodPriceRequest()
                    .setData(List.of(
                            new SetGoodDto()
                                    .setNmID(advertToStart.getItemId())
                                    .setPrice(sessionStorage.get(chatId).getNewCardPrice())
                                    .setDiscount(sessionStorage.get(chatId).getNewCardDiscount())
                    ));
            wildberriesIntegrationService.setGoodPriceAndDiscount(setGoodPriceRequest);
        }

        AdvertUploadPhotoRequest uploadPhotoRequest = new AdvertUploadPhotoRequest()
                .setNmId(advertToStart.getItemId())
                .setPhotoNumber(CARD_MAIN_PHOTO_POSITION)
                .setUploadFile(sessionStorage.get(chatId).getUploadedCardPhoto());
        wildberriesIntegrationService.uploadPhoto(uploadPhotoRequest);

        ChangeStocksRequest changeStocksRequest = new ChangeStocksRequest().setStocks(List.of(new SkuDto()
                .setSku(card.getBarcode())
                .setAmount(changeStocksCount)));
        wildberriesIntegrationService.changeStocks(changeStocksRequest);

        wildberriesIntegrationService.startAdvert(advertId);

        LocalDateTime startDateTime = LocalDateTime.now();
        advertToStart.setNextCheckDateTime(startDateTime.plusSeconds(checkMilliseconds / 1000));
        advertToStart.setStartCheckDateTime(startDateTime);
        advertToStart.setStatus(AdvertStatus.RUNNING);
        advertToStart.setName(advertStartName);
        advertToStart.setChatId(chatId);
        AdvertBean updatedAdvert = advertService.update(advertToStart);

        AdvertEntity advertEntity = advertService.verifyExistsByAdvertId(advertId);
        AdvertStatisticEntity advertStatisticEntity = new AdvertStatisticEntity()
                .setActive(true)
                .setPhoto(sessionStorage.get(chatId).getUploadedCardPhoto())
                .setAdvertEntity(advertEntity)
                .setClient(client);
        advertStatisticRepository.save(advertStatisticEntity);

        iterationsCounterService.add(advertToStart.getAdvertId());
        sessionStorage.close(chatId);

        logger.info("Successful start advert = [{}]", updatedAdvert);
        return advertToStart;
    }
}
