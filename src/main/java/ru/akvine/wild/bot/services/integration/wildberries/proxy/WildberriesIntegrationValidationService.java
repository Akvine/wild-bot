package ru.akvine.wild.bot.services.integration.wildberries.proxy;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.enums.ProxyType;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.*;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.CardDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.ChangeStocksRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.type.CardTypeResponse;

/**
 * Единая точка для валидации входные и выходных данных
 */
@Service
public class WildberriesIntegrationValidationService extends WildberriesIntegrationServiceProxy {
    @Override
    public ProxyType getType() {
        return ProxyType.VALIDATION;
    }

    @Override
    public List<CardDto> getCards(String apiToken) {
        validateToken(apiToken);
        return targetObject.getCards(apiToken);
    }

    @Override
    public AdvertListResponse getAdverts(String apiToken) {
        validateToken(apiToken);
        return targetObject.getAdverts(apiToken);
    }

    @Override
    public AdvertBudgetInfoResponse getAdvertBudgetInfo(int advertId, String apiToken) {
        validateToken(apiToken);
        return targetObject.getAdvertBudgetInfo(advertId, apiToken);
    }

    @Override
    public AdvertBudgetDepositResponse advertBudgetDeposit(int advertId, int sum, String apiToken) {
        validateToken(apiToken);
        return targetObject.advertBudgetDeposit(advertId, sum, apiToken);
    }

    @Override
    public void startAdvert(int advertId, String apiToken) {
        validateToken(apiToken);
        targetObject.startAdvert(advertId, apiToken);
    }

    @Override
    public AdvertsInfoResponse getAdvertsInfo(List<Integer> advertIds, String apiToken) {
        validateToken(apiToken);
        return targetObject.getAdvertsInfo(advertIds, apiToken);
    }

    @Override
    public AdvertStatisticResponse getAdvertStatistic(String advertId, String apiToken) {
        validateToken(apiToken);
        return targetObject.getAdvertStatistic(advertId, apiToken);
    }

    @Override
    public void pauseAdvert(int advertId, String apiToken) {
        validateToken(apiToken);
        targetObject.pauseAdvert(advertId, apiToken);
    }

    @Override
    public void changeAdvertCpm(AdvertChangeCpmRequest request, String apiToken) {
        validateToken(apiToken);
        targetObject.changeAdvertCpm(request, apiToken);
    }

    @Override
    public void renameAdvert(int advertId, String name, String apiToken) {
        validateToken(apiToken);
        targetObject.renameAdvert(advertId, name, apiToken);
    }

    @Override
    public AdvertUploadPhotoResponse uploadPhoto(AdvertUploadPhotoRequest request, String apiToken) {
        validateToken(apiToken);
        return targetObject.uploadPhoto(request, apiToken);
    }

    @Override
    public void changeStocks(ChangeStocksRequest request, int warehouseId, String apiToken) {
        validateToken(apiToken);
        targetObject.changeStocks(request, warehouseId, apiToken);
    }

    @Override
    public AdvertFullStatisticResponse[] getAdvertsFullStatisticByDates(
            List<AdvertFullStatisticDatesDto> request, String apiToken) {
        validateToken(apiToken);
        AdvertFullStatisticResponse[] responses = targetObject.getAdvertsFullStatisticByDates(request, apiToken);

        if (responses == null || responses.length == 0) {
            throw new IntegrationException("Full statistic responses by dates is null or empty");
        }

        return responses;
    }

    @Override
    public AdvertFullStatisticResponse[] getAdvertsFullStatisticByInterval(
            List<AdvertFullStatisticIntervalDto> request, String apiToken) {
        validateToken(apiToken);

        AdvertFullStatisticResponse[] responses = targetObject.getAdvertsFullStatisticByInterval(request, apiToken);

        if (responses == null || responses.length == 0) {
            throw new IntegrationException("Full statistic responses by interval is null or empty");
        }
        return responses;
    }

    @Override
    public GetGoodsResponse getGoods(GetGoodsRequest request, String apiToken) {
        validateToken(apiToken);
        GetGoodsResponse response = targetObject.getGoods(request, apiToken);

        if (response == null) {
            throw new IntegrationException("Get goods response is null");
        }

        return response;
    }

    @Override
    public void setGoodPriceAndDiscount(SetGoodPriceRequest request, String apiToken) {
        validateToken(apiToken);
        targetObject.setGoodPriceAndDiscount(request, apiToken);
    }

    @Override
    public int createAdvert(AdvertCreateRequest request, String apiToken) {
        validateToken(apiToken);
        return targetObject.createAdvert(request, apiToken);
    }

    @Override
    public CardTypeResponse getTypes(String apiToken) {
        validateToken(apiToken);
        CardTypeResponse response = targetObject.getTypes(apiToken);

        if (Boolean.parseBoolean(response.getError())) {
            String errorMessage = String.format(
                    "Error while getting card types! Error = [%s]. Message = [%s]",
                    response.getError(), response.getErrorText());
            throw new IntegrationException(errorMessage);
        }

        return response;
    }

    private void validateToken(String apiToken) {
        if (StringUtils.isBlank(apiToken)) {
            throw new IntegrationException("apiToken is null");
        }
    }
}
