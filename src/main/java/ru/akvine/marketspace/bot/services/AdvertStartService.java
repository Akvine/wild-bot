package ru.akvine.marketspace.bot.services;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.entities.AdvertEntity;
import ru.akvine.marketspace.bot.entities.CardEntity;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.infrastructure.SessionStorage;
import ru.akvine.marketspace.bot.infrastructure.impl.ClientSessionData;
import ru.akvine.marketspace.bot.services.domain.AdvertBean;
import ru.akvine.marketspace.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertBudgetInfoResponse;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertChangeCpmRequest;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertUploadPhotoRequest;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.ChangeStocksRequest;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.SkuDto;
import ru.akvine.marketspace.bot.utils.DateUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertStartService {
    private final AdvertService advertService;
    private final CardService cardService;
    private final WildberriesIntegrationService wildberriesIntegrationService;
    private final IterationsCounterService iterationsCounterService;
    private final SessionStorage<String, ClientSessionData> sessionStorage;

    @Value("${check.advert.cron.milliseconds}")
    private long checkMilliseconds;

    @Value("${default.advert.budget.sum.increase}")
    private int defaultBudgetSumIncrease;
    @Value("${default.advert.cpm}")
    private int defaultCpm;
    @Value("${wildberries.change.stocks.count}")
    private int changeStocksCount;
    @Value("${advert.budget.min.sum}")
    private int budgetMinSum;

    private final static int CARD_MAIN_PHOTO_POSITION = 1;

    public AdvertBean startByAdvertId(String chatId, String advertId) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        Preconditions.checkNotNull(advertId, "advertId is null");
        String categoryId = sessionStorage.get(chatId).getChoosenCategoryId();
        logger.info("Try to start advert advert with id = {} and category with id = {}", advertId, categoryId);

        AdvertEntity advertEntity = advertService.verifyExistsByAdvertId(advertId);
        return startInternal(chatId, new AdvertBean(advertEntity));
    }

    public AdvertBean start(String chatId) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        String categoryId = sessionStorage.get(chatId).getChoosenCategoryId();
        logger.info("Try to start first one advert with category id = {}", categoryId);
        AdvertBean advertToStart = advertService.getFirst(categoryId);
        return startInternal(chatId, advertToStart);
    }

    private AdvertBean startInternal(String chatId, AdvertBean advertToStart) {
        CardEntity card = cardService.verifyExistsByItemId(advertToStart.getItemId());

        String advertId = advertToStart.getAdvertId();
        AdvertBudgetInfoResponse advertBudgetInfo = wildberriesIntegrationService.getAdvertBudgetInfo(advertId);
        Integer advertTotalBudget = advertBudgetInfo.getTotal();

        if (advertTotalBudget < budgetMinSum) {
            wildberriesIntegrationService.advertBudgetDeposit(advertId, defaultBudgetSumIncrease);
            advertToStart.plusStartBudget(defaultBudgetSumIncrease);
        } else {
            advertToStart.setStartBudgetSum(advertTotalBudget);
        }
        advertToStart.setCheckBudgetSum(advertToStart.getStartBudgetSum());

        if (advertToStart.getCpm() < defaultCpm) {
            AdvertChangeCpmRequest request = new AdvertChangeCpmRequest()
                    .setAdvertId(Integer.parseInt(advertId))
                    .setType(advertToStart.getType().getCode())
                    .setParam(Integer.parseInt(advertToStart.getCategoryId()))
                    .setCpm(defaultCpm);
            advertToStart.setCpm(defaultCpm);
            wildberriesIntegrationService.changeAdvertCpm(request);
        }

        String advertStartName = "Bot:" + DateUtils.formatLocalDateTime(LocalDateTime.now()) + ":" + advertId;
        wildberriesIntegrationService.renameAdvert(advertId, advertStartName);

        AdvertUploadPhotoRequest request = new AdvertUploadPhotoRequest()
                .setNmId(advertToStart.getItemId())
                .setPhotoNumber(CARD_MAIN_PHOTO_POSITION)
                .setUploadFile(sessionStorage.get(chatId).getUploadedCardPhoto());
        wildberriesIntegrationService.uploadPhoto(request);

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

        iterationsCounterService.add(advertToStart.getAdvertId());

        logger.info("Successful start advert = [{}]", updatedAdvert);
        return advertToStart;
    }
}
