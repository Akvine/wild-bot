package ru.akvine.wild.bot.services.integration.wildberries.proxy;

import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.enums.ProxyType;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.*;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.CardDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.ChangeStocksRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.type.CardTypeResponse;

import java.util.List;
import java.util.Random;

/**
 * Сервис для тестирования нестабильных ситуаций при взаимодействии с внешним сервисом на тестовых стендах или локально.
 */
@Service
public class WildberriesIntegrationUnstableService extends WildberriesIntegrationServiceProxy {
    private Random random = new Random();

    @Override
    public ProxyType getType() {
        return ProxyType.THROW_EXCEPTION_RANDOMLY;
    }

    @Override
    public List<CardDto> getCards(String apiToken) {
        boolean throwException = random.nextBoolean();
        if (throwException) {
            throw new IntegrationException("Error with communicate to service");
        }

        return targetObject.getCards(apiToken);
    }

    @Override
    public AdvertListResponse getAdverts(String apiToken) {
        boolean throwException = random.nextBoolean();
        if (throwException) {
            throw new IntegrationException("Error with communicate to service");
        }

        return targetObject.getAdverts(apiToken);
    }

    @Override
    public AdvertBudgetInfoResponse getAdvertBudgetInfo(int advertId, String apiToken) {
        boolean throwException = random.nextBoolean();
        if (throwException) {
            throw new IntegrationException("Error with communicate to service");
        }

        return targetObject.getAdvertBudgetInfo(advertId, apiToken);
    }

    @Override
    public AdvertBudgetDepositResponse advertBudgetDeposit(int advertId, int sum, String apiToken) {
        boolean throwException = random.nextBoolean();
        if (throwException) {
            throw new IntegrationException("Error with communicate to service");
        }

        return targetObject.advertBudgetDeposit(advertId, sum, apiToken);
    }

    @Override
    public void startAdvert(int advertId, String apiToken) {
        boolean throwException = random.nextBoolean();
        if (throwException) {
            throw new IntegrationException("Error with communicate to service");
        }

        targetObject.startAdvert(advertId, apiToken);
    }

    @Override
    public AdvertsInfoResponse getAdvertsInfo(List<Integer> advertIds, String apiToken) {
        boolean throwException = random.nextBoolean();
        if (throwException) {
            throw new IntegrationException("Error with communicate to service");
        }

        return targetObject.getAdvertsInfo(advertIds, apiToken);
    }

    @Override
    public AdvertStatisticResponse getAdvertStatistic(String advertId, String apiToken) {
        boolean throwException = random.nextBoolean();
        if (throwException) {
            throw new IntegrationException("Error with communicate to service");
        }

        return targetObject.getAdvertStatistic(advertId, apiToken);
    }

    @Override
    public void pauseAdvert(int advertId, String apiToken) {
        boolean throwException = random.nextBoolean();
        if (throwException) {
            throw new IntegrationException("Error with communicate to service");
        }

        targetObject.pauseAdvert(advertId, apiToken);
    }

    @Override
    public void changeAdvertCpm(AdvertChangeCpmRequest request, String apiToken) {
        boolean throwException = random.nextBoolean();
        if (throwException) {
            throw new IntegrationException("Error with communicate to service");
        }

        targetObject.changeAdvertCpm(request, apiToken);
    }

    @Override
    public void renameAdvert(int advertId, String name, String apiToken) {
        boolean throwException = random.nextBoolean();
        if (throwException) {
            throw new IntegrationException("Error with communicate to service");
        }

        targetObject.renameAdvert(advertId, name, apiToken);
    }

    @Override
    public AdvertUploadPhotoResponse uploadPhoto(AdvertUploadPhotoRequest request, String apiToken) {
        boolean throwException = random.nextBoolean();
        if (throwException) {
            throw new IntegrationException("Error with communicate to service");
        }

        return targetObject.uploadPhoto(request, apiToken);
    }

    @Override
    public void changeStocks(ChangeStocksRequest request, int warehouseId, String apiToken) {
        boolean throwException = random.nextBoolean();
        if (throwException) {
            throw new IntegrationException("Error with communicate to service");
        }

        targetObject.changeStocks(request, warehouseId, apiToken);
    }

    @Override
    public AdvertFullStatisticResponse[] getAdvertsFullStatisticByDates(List<AdvertFullStatisticDatesDto> request, String apiToken) {
        boolean throwException = random.nextBoolean();
        if (throwException) {
            throw new IntegrationException("Error with communicate to service");
        }

        return targetObject.getAdvertsFullStatisticByDates(request, apiToken);
    }

    @Override
    public AdvertFullStatisticResponse[] getAdvertsFullStatisticByInterval(List<AdvertFullStatisticIntervalDto> request, String apiToken) {
        boolean throwException = random.nextBoolean();
        if (throwException) {
            throw new IntegrationException("Error with communicate to service");
        }

        return targetObject.getAdvertsFullStatisticByInterval(request, apiToken);
    }

    @Override
    public GetGoodsResponse getGoods(GetGoodsRequest request, String apiToken) {
        boolean throwException = random.nextBoolean();
        if (throwException) {
            throw new IntegrationException("Error with communicate to service");
        }

        return targetObject.getGoods(request, apiToken);
    }

    @Override
    public void setGoodPriceAndDiscount(SetGoodPriceRequest request, String apiToken) {
        boolean throwException = random.nextBoolean();
        if (throwException) {
            throw new IntegrationException("Error with communicate to service");
        }

        targetObject.setGoodPriceAndDiscount(request, apiToken);
    }

    @Override
    public int createAdvert(AdvertCreateRequest request, String apiToken) {
        boolean throwException = random.nextBoolean();
        if (throwException) {
            throw new IntegrationException("Error with communicate to service");
        }

        return targetObject.createAdvert(request, apiToken);
    }

    @Override
    public CardTypeResponse getTypes(String apiToken) {
        boolean throwException = random.nextBoolean();
        if (throwException) {
            throw new IntegrationException("Error with communicate to service");
        }

        return targetObject.getTypes(apiToken);
    }
}
