package ru.akvine.wild.bot.services.integration.wildberries.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import ru.akvine.wild.bot.enums.ProxyType;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationServiceOrigin;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.*;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.CardDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.ChangeStocksRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.type.CardTypeResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationServiceOrigin.WildberriesApiMethods.*;

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

        List<CardDto> cards;
        try {
            cards = targetObject.getCards();

            timeMeter.stop();
            double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

            logExecutionTime(GET_CARD_LIST, executionTime);
            return cards;
        } catch (Exception exception) {
            throw new IntegrationException(exception);
        } finally {
            timeMeter.stop();
        }
    }

    @Override
    public AdvertListResponse getAdverts() {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();

        AdvertListResponse response;
        try {
            response = targetObject.getAdverts();

            timeMeter.stop();
            double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

            logExecutionTime(GET_ADVERTS, executionTime);
            return response;
        } catch (Exception exception) {
            throw new IntegrationException(exception);
        } finally {
            timeMeter.stop();
        }

    }

    @Override
    public AdvertBudgetInfoResponse getAdvertBudgetInfo(int advertId) {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();

        AdvertBudgetInfoResponse response = targetObject.getAdvertBudgetInfo(advertId);

        timeMeter.stop();
        double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

        logExecutionTime(ADVERT_BUDGET_INFO, executionTime);
        return response;
    }

    @Override
    public AdvertBudgetDepositResponse advertBudgetDeposit(int advertId, int sum) {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();

        AdvertBudgetDepositResponse response;
        try {
            response = targetObject.advertBudgetDeposit(advertId, sum);

            timeMeter.stop();
            double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

            logExecutionTime(ADVERT_BUDGET_DEPOSIT, executionTime);
            return response;
        } catch (Exception exception) {
            throw new IntegrationException(exception);
        } finally {
            timeMeter.stop();
        }

    }

    @Override
    public void startAdvert(int advertId) {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();

        try {
            targetObject.startAdvert(advertId);

            timeMeter.stop();
            double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

            logExecutionTime(START_ADVERT, executionTime);
        } catch (Exception exception) {
            throw new IntegrationException(exception);
        } finally {
            timeMeter.stop();
        }

    }

    @Override
    public AdvertsInfoResponse getAdvertsInfo(List<Integer> advertIds) {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();

        AdvertsInfoResponse response;
        try {
            response = targetObject.getAdvertsInfo(advertIds);

            timeMeter.stop();
            double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

            logExecutionTime(GET_ADVERTS_INFO, executionTime);
            return response;
        } catch (Exception exception) {
            throw new IntegrationException(exception);
        } finally {
            timeMeter.stop();
        }
    }

    @Override
    public AdvertStatisticResponse getAdvertStatistic(String advertId) {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();

        AdvertStatisticResponse response;
        try {
            response = targetObject.getAdvertStatistic(advertId);

            timeMeter.stop();
            double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

            logExecutionTime(GET_ADVERT_STATISTIC, executionTime);
            return response;
        } catch (Exception exception) {
            throw new IntegrationException(exception);
        } finally {
            timeMeter.stop();
        }
    }

    @Override
    public void pauseAdvert(int advertId) {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();

        try {
            targetObject.pauseAdvert(advertId);
            timeMeter.stop();
            double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

            logExecutionTime(PAUSE_ADVERT, executionTime);
        } catch (Exception exception) {
            throw new IntegrationException(exception);
        } finally {
            timeMeter.stop();
        }
    }

    @Override
    public void changeAdvertCpm(AdvertChangeCpmRequest request) {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();

        try {
            targetObject.changeAdvertCpm(request);

            timeMeter.stop();
            double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

            logExecutionTime(CHANGE_ADVERT_CPM, executionTime);
        } catch (Exception exception) {
            throw new IntegrationException(exception);
        } finally {
            timeMeter.stop();
        }

    }

    @Override
    public void renameAdvert(int advertId, String name) {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();

        try {
            targetObject.renameAdvert(advertId, name);

            timeMeter.stop();
            double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

            logExecutionTime(RENAME_ADVERT, executionTime);
        } catch (Exception exception) {
            throw new IntegrationException(exception);
        } finally {
            timeMeter.stop();
        }
    }

    @Override
    public AdvertUploadPhotoResponse uploadPhoto(AdvertUploadPhotoRequest request) {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();

        AdvertUploadPhotoResponse response;
        try {
            response = targetObject.uploadPhoto(request);

            timeMeter.stop();
            double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

            logExecutionTime(UPLOAD_CARD_PHOTO, executionTime);
            return response;
        } catch (Exception exception) {
            throw new IntegrationException(exception);
        } finally {
            timeMeter.stop();
        }
    }

    @Override
    public void changeStocks(ChangeStocksRequest request, int warehouseId) {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();

        try {
            targetObject.changeStocks(request, warehouseId);

            timeMeter.stop();
            double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

            logExecutionTime(CHANGE_CARD_STOCKS, executionTime);
        } catch (Exception exception) {
            throw new IntegrationException(exception);
        } finally {
            timeMeter.stop();
        }
    }

    @Override
    public AdvertFullStatisticResponse[] getAdvertsFullStatisticByDates(List<AdvertFullStatisticDatesDto> request) {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();

        AdvertFullStatisticResponse[] response;
        try {
            response = targetObject.getAdvertsFullStatisticByDates(request);

            timeMeter.stop();
            double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

            logExecutionTime(GET_ADVERTS_FULL_STATISTIC, executionTime);
            return response;
        } catch (Exception exception) {
            throw new IntegrationException(exception);
        } finally {
            timeMeter.stop();
        }
    }

    @Override
    public AdvertFullStatisticResponse[] getAdvertsFullStatisticByInterval(
            List<AdvertFullStatisticIntervalDto> request) {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();

        AdvertFullStatisticResponse[] response;
        try {
            response = targetObject.getAdvertsFullStatisticByInterval(request);

            timeMeter.stop();
            double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

            logExecutionTime(GET_ADVERTS_FULL_STATISTIC, executionTime);
            return response;
        } catch (Exception exception) {
            throw new IntegrationException(exception);
        } finally {
            timeMeter.stop();
        }
    }

    @Override
    public GetGoodsResponse getGoods(GetGoodsRequest request) {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();

        GetGoodsResponse response;
        try {
            response = targetObject.getGoods(request);

            timeMeter.stop();
            double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

            logExecutionTime(GET_GOODS, executionTime);
            return response;
        } catch (Exception exception) {
            throw new IntegrationException(exception);
        } finally {
            timeMeter.stop();
        }
    }

    @Override
    public void setGoodPriceAndDiscount(SetGoodPriceRequest request) {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();

        try {
            targetObject.setGoodPriceAndDiscount(request);

            timeMeter.stop();
            double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

            logExecutionTime(SET_GOODS_PRICE_AND_DISCOUNT, executionTime);
        } catch (Exception exception) {
            throw new IntegrationException(exception);
        } finally {
            timeMeter.stop();
        }
    }

    @Override
    public int createAdvert(AdvertCreateRequest request) {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();

        try {
            int response = targetObject.createAdvert(request);

            timeMeter.stop();
            double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

            logExecutionTime(SET_GOODS_PRICE_AND_DISCOUNT, executionTime);
            return response;
        } catch (Exception exception) {
            throw new IntegrationException(exception);
        } finally {
            timeMeter.stop();
        }
    }

    @Override
    public CardTypeResponse getTypes() {
        StopWatch timeMeter = new StopWatch();
        timeMeter.start();
        try {
            CardTypeResponse response = targetObject.getTypes();

            timeMeter.stop();
            double executionTime = timeMeter.getTotalTime(TimeUnit.SECONDS);

            logExecutionTime(GET_CARD_TYPES, executionTime);
            return response;
        } catch (Exception exception) {
            throw new IntegrationException(exception);
        } finally {
            timeMeter.stop();
        }
    }

    private void logExecutionTime(WildberriesIntegrationServiceOrigin.WildberriesApiMethods apiMethod, double executionTime) {
        logger.info("[{}] api method execution time seconds: {}", apiMethod, executionTime);
    }
}
