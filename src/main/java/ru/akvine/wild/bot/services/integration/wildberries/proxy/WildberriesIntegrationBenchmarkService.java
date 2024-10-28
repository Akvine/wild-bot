package ru.akvine.wild.bot.services.integration.wildberries.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import ru.akvine.wild.bot.enums.ProxyType;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.*;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.CardDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.ChangeStocksRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.type.CardTypeResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class WildberriesIntegrationBenchmarkService extends WildberriesIntegrationServiceProxy {
    @Override
    public ProxyType getType() {
        return ProxyType.BENCHMARK;
    }

    @Override
    public List<CardDto> getCards() {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();

        List<CardDto> cards = targetObject.getCards();

        timeMeter.stop();
        double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

        logger.info("Get cards execution time seconds: {}", executionTime);
        return cards;
    }

    @Override
    public AdvertListResponse getAdverts() {
        return null;
    }

    @Override
    public AdvertBudgetInfoResponse getAdvertBudgetInfo(int advertId) {
        return null;
    }

    @Override
    public AdvertBudgetDepositResponse advertBudgetDeposit(int advertId, int sum) {
        return null;
    }

    @Override
    public void startAdvert(int advertId) {

    }

    @Override
    public AdvertsInfoResponse getAdvertsInfo(List<Integer> advertIds) {
        return null;
    }

    @Override
    public AdvertStatisticResponse getAdvertStatistic(String advertId) {
        return null;
    }

    @Override
    public void pauseAdvert(int advertId) {

    }

    @Override
    public void changeAdvertCpm(AdvertChangeCpmRequest request) {

    }

    @Override
    public void renameAdvert(int advertId, String name) {

    }

    @Override
    public AdvertUploadPhotoResponse uploadPhoto(AdvertUploadPhotoRequest request) {
        return null;
    }

    @Override
    public void changeStocks(ChangeStocksRequest request, int warehouseId) {

    }

    @Override
    public AdvertFullStatisticResponse[] getAdvertsFullStatisticByDates(List<AdvertFullStatisticDatesDto> request) {
        return new AdvertFullStatisticResponse[0];
    }

    @Override
    public AdvertFullStatisticResponse[] getAdvertsFullStatisticByInterval(List<AdvertFullStatisticIntervalDto> request) {
        return new AdvertFullStatisticResponse[0];
    }

    @Override
    public GetGoodsResponse getGoods(GetGoodsRequest request) {
        return null;
    }

    @Override
    public void setGoodPriceAndDiscount(SetGoodPriceRequest request) {

    }

    @Override
    public int createAdvert(AdvertCreateRequest request) {
        return 0;
    }

    @Override
    public CardTypeResponse getTypes() {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();
        try {
            CardTypeResponse response = targetObject.getTypes();

            timeMeter.stop();
            double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);
            logger.info("Get cards types execution time seconds: {}", executionTime);
            return response;
        } catch (IntegrationException exception) {
            timeMeter.stop();
            throw new IntegrationException(exception);
        } finally {
            timeMeter.stop();
        }
    }
}
