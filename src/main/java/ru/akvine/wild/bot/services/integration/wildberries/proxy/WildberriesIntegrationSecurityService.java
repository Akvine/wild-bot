package ru.akvine.wild.bot.services.integration.wildberries.proxy;

import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.enums.ProxyType;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.*;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.CardDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.ChangeStocksRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.type.CardTypeResponse;

import java.util.List;

@Service
public class WildberriesIntegrationSecurityService extends WildberriesIntegrationServiceProxy {

    @Override
    public List<CardDto> getCards() {
        return List.of();
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
        System.out.println("System security");
        return targetObject.getTypes();
    }

    @Override
    public ProxyType getType() {
        return ProxyType.SECURITY;
    }
}
