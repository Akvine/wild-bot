package ru.akvine.wild.bot.services.integration.wildberries;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.*;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.CardDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.ChangeStocksRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.type.CardTypeResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("local")
public class WildberriesIntegrationServiceEmulator implements WildberriesIntegrationService {
    @Value("${wildberries.api.token}")
    private String apiToken;

    private final RestTemplate restTemplate = new RestTemplate();

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
        logger.info("Get card types");

        HttpHeaders headers = buildHttpHeadersInternal();
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<CardTypeResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    WildberriesApiMethods.GET_CARD_TYPES.getUrl() + WildberriesApiMethods.GET_CARD_TYPES.getMethod(),
                    HttpMethod.GET,
                    httpEntity,
                    CardTypeResponse.class
            );
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.GET_CARD_TYPES, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        return responseEntity.getBody();
    }

    private HttpHeaders buildHttpHeadersInternal() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, apiToken);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    @Getter
    @AllArgsConstructor
    private enum WildberriesApiMethods {
        GET_CARD_TYPES("http://localhost:8083/api/cards/types", "/content/v2/directory/kinds");

        private final String url;
        private final String method;
    }
}
