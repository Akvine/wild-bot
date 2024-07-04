package ru.akvine.marketspace.bot.services;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.context.ClientDataContext;
import ru.akvine.marketspace.bot.entities.AdvertEntity;
import ru.akvine.marketspace.bot.entities.CardEntity;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
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
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertStartService {
    private final AdvertService advertService;
    private final CardService cardService;
    private final WildberriesIntegrationService wildberriesIntegrationService;
    private final IterationsCounterService iterationsCounterService;

    @Value("${check.advert.cron.milliseconds}")
    private long checkMilliseconds;

    @Value("${default.start.advert.budget.sum}")
    private int defaultBudgetSum;
    @Value("${default.start.advert.cpm.sum}")
    private int defaultCpmSum;
    @Value("${wildberries.change.stocks.count}")
    private int changeStocksCount;

    private final static int CARD_MAIN_PHOTO_POSITION = 1;

    public AdvertBean startByAdvertId(String advertId) {
        Preconditions.checkNotNull(advertId, "advertId is null");
        String categoryId = Objects.requireNonNull(ClientDataContext.get()).getCategoryId();
        logger.info("Try to start advert advert with id = {} and category with id = {}", advertId, categoryId);

        AdvertEntity advertEntity = advertService.verifyExistsByAdvertId(advertId);
        return startInternal(new AdvertBean(advertEntity));
    }

    public AdvertBean start() {
        String categoryId = Objects.requireNonNull(ClientDataContext.get()).getCategoryId();
        logger.info("Try to start advert randomly with category id = {}", categoryId);
        AdvertBean advertToStart = advertService.getFirst(categoryId);
        return startInternal(advertToStart);
    }

    private AdvertBean startInternal(AdvertBean advertToStart) {
        CardEntity card = cardService.verifyExistsByItemId(advertToStart.getItemId());

        String advertId = advertToStart.getAdvertId();
        AdvertBudgetInfoResponse advertBudgetInfo = wildberriesIntegrationService.getAdvertBudgetInfo(advertId);
        Integer advertTotalBudget = advertBudgetInfo.getTotal();

        if (advertTotalBudget == 0) {
            wildberriesIntegrationService.advertBudgetDeposit(advertId, defaultBudgetSum);
            advertToStart.setStartBudgetSum(defaultBudgetSum);
        } else {
            advertToStart.setStartBudgetSum(advertTotalBudget);
        }
        advertToStart.setCheckBudgetSum(advertToStart.getStartBudgetSum());

        if (advertToStart.getCpm() < defaultCpmSum) {
            AdvertChangeCpmRequest request = new AdvertChangeCpmRequest()
                    .setAdvertId(Integer.parseInt(advertId))
                    .setType(advertToStart.getType().getCode())
                    .setParam(Integer.parseInt(advertToStart.getCategoryId()))
                    .setCpm(defaultCpmSum);
            advertToStart.setCpm(defaultCpmSum);
            wildberriesIntegrationService.changeAdvertCpm(request);
        }

        String advertStartName = "Bot:" + DateUtils.formatLocalDateTime(LocalDateTime.now(), DateUtils.DATE_TIME_FORMATTER_WITHOUT_MILLISECONDS) + ":" + advertId;
        wildberriesIntegrationService.renameAdvert(advertId, advertStartName);

        AdvertUploadPhotoRequest request = new AdvertUploadPhotoRequest()
                .setNmId(advertToStart.getItemId())
                .setPhotoNumber(CARD_MAIN_PHOTO_POSITION)
                .setUploadFile(Objects.requireNonNull(ClientDataContext.get()).getNewCardPhoto());
        wildberriesIntegrationService.uploadPhoto(request);

        ChangeStocksRequest changeStocksRequest = new ChangeStocksRequest().setStocks(List.of(new SkuDto()
                        .setSku(card.getBarcode())
                        .setAmount(changeStocksCount)));
        wildberriesIntegrationService.changeStocks(changeStocksRequest);

        // TODO : нереальный кастыль. Нужно посмотреть на API WB
        try {
            Thread.sleep(5000);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        wildberriesIntegrationService.startAdvert(advertId);

        LocalDateTime startDateTime = LocalDateTime.now();
        advertToStart.setNextCheckDateTime(startDateTime.plusSeconds(checkMilliseconds / 1000));
        advertToStart.setStartCheckDateTime(startDateTime);
        advertToStart.setStatus(AdvertStatus.RUNNING);
        advertToStart.setName(advertStartName);
        AdvertBean updatedAdvert = advertService.update(advertToStart);

        iterationsCounterService.add(advertToStart.getAdvertId());

        logger.info("Successful start advert = [{}]", updatedAdvert);
        return advertToStart;
    }
}
