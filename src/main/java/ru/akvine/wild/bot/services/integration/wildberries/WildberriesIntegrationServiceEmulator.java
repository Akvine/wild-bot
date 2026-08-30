package ru.akvine.wild.bot.services.integration.wildberries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.akvine.wild.bot.constants.ProxyConstants;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.services.EncryptionService;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.*;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.CardDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.CardListResponse;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.ChangeStocksRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.type.CardTypeResponse;
import ru.akvine.wild.bot.utils.RequestUtils;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("local")
@Qualifier(ProxyConstants.ORIGIN_BEAN_NAME)
public class WildberriesIntegrationServiceEmulator implements WildberriesIntegrationService {
    private final EncryptionService encryptionService;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String ADVERT_QUERY_ID_PARAM = "id";
    private static final String FILTER_NM_ID_QUERY_PARAM = "filterNmID";

    @Override
    public List<CardDto> getCards(String apiToken) {
        logger.info("Get cards");

        HttpHeaders headers = buildHttpHeadersForJsonBody(apiToken);
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<CardListResponse> responseEntity;
        try {
            responseEntity = restTemplate.postForEntity(
                    WildberriesApiMethods.GET_CARD_LIST.getUrl() + WildberriesApiMethods.GET_CARD_LIST.getMethod(),
                    httpEntity,
                    CardListResponse.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.GET_CARD_LIST, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
        return Objects.requireNonNull(responseEntity.getBody()).getCards();
    }

    @Override
    public AdvertListResponse getAdverts(String apiToken) {
        logger.info("Get adverts list");
        HttpHeaders headers = buildHttpHeadersForJsonBody(apiToken);
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<AdvertListResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    WildberriesApiMethods.GET_ADVERTS.getUrl() + WildberriesApiMethods.GET_ADVERTS.getMethod(),
                    HttpMethod.GET,
                    httpEntity,
                    AdvertListResponse.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.GET_ADVERTS, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        AdvertListResponse response = responseEntity.getBody();
        logger.info("Get adverts list response was occurred with elements size = {}", response.getAll());
        return response;
    }

    @Override
    public AdvertBudgetInfoResponse getAdvertBudgetInfo(int advertId, String apiToken) {
        logger.info("Get budget info for advert with id = {}", advertId);
        HttpHeaders headers = buildHttpHeadersForJsonBody(apiToken);
        HttpEntity<String> httpEntity = new HttpEntity<>("some", headers);

        Map<String, String> queryParams = Map.of(ADVERT_QUERY_ID_PARAM, String.valueOf(advertId));
        ResponseEntity<AdvertBudgetInfoResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    RequestUtils.buildUri(
                            WildberriesApiMethods.ADVERT_BUDGET_INFO.getUrl()
                                    + WildberriesApiMethods.ADVERT_BUDGET_INFO.getMethod(),
                            queryParams),
                    HttpMethod.GET,
                    httpEntity,
                    AdvertBudgetInfoResponse.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.ADVERT_BUDGET_INFO, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        AdvertBudgetInfoResponse response = responseEntity.getBody();
        logger.info("Advert budget info response was occurred = [{}]", response);
        return response;
    }

    @Override
    public AdvertBudgetDepositResponse advertBudgetDeposit(int advertId, int sum, String apiToken) {
        logger.info("Deposit budget advert with id = {} and sum = {}", advertId, sum);
        AdvertBudgetDepositRequest request =
                new AdvertBudgetDepositRequest().setReturn(true).setType(0).setSum(sum);

        HttpHeaders headers = buildHttpHeadersForJsonBody(apiToken);
        HttpEntity<AdvertBudgetDepositRequest> httpEntity = new HttpEntity<>(request, headers);

        Map<String, String> queryParams = Map.of(ADVERT_QUERY_ID_PARAM, String.valueOf(advertId));
        ResponseEntity<AdvertBudgetDepositResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    RequestUtils.buildUri(
                            WildberriesApiMethods.ADVERT_BUDGET_DEPOSIT.getUrl()
                                    + WildberriesApiMethods.ADVERT_BUDGET_DEPOSIT.getMethod(),
                            queryParams),
                    HttpMethod.POST,
                    httpEntity,
                    AdvertBudgetDepositResponse.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = %s",
                    WildberriesApiMethods.ADVERT_BUDGET_DEPOSIT, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        AdvertBudgetDepositResponse response = responseEntity.getBody();
        logger.info("Advert budget deposit response was occurred = [{}]", response);
        return response;
    }

    @Override
    public void startAdvert(int advertId, String apiToken) {
        logger.info("Start advert with id = {}", advertId);
        HttpHeaders headers = buildHttpHeadersForJsonBody(apiToken);
        HttpEntity<String> httpEntity = new HttpEntity<>("some", headers);

        Map<String, String> queryParams = Map.of(ADVERT_QUERY_ID_PARAM, String.valueOf(advertId));
        try {
            restTemplate.exchange(
                    RequestUtils.buildUri(
                            WildberriesApiMethods.START_ADVERT.getUrl()
                                    + WildberriesApiMethods.START_ADVERT.getMethod(),
                            queryParams),
                    HttpMethod.GET,
                    httpEntity,
                    String.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.START_ADVERT, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }

    @Override
    public AdvertsInfoResponse getAdvertsInfo(List<Integer> advertIds, String apiToken) {
        logger.info("Get adverts info");
        HttpHeaders headers = buildHttpHeadersForJsonBody(apiToken);
        HttpEntity<List<Integer>> httpEntity = new HttpEntity<>(advertIds, headers);

        ResponseEntity<AdvertSubListResponse> responseEntity;
        try {
            responseEntity = restTemplate.postForEntity(
                    WildberriesApiMethods.GET_ADVERTS_INFO.getUrl()
                            + WildberriesApiMethods.GET_ADVERTS_INFO.getMethod(),
                    httpEntity,
                    AdvertSubListResponse.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.GET_ADVERTS_INFO, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        List<AdvertDto> response =
                List.of(Objects.requireNonNull(responseEntity.getBody().getAdverts()));
        logger.info("Get adverts info response with size = {} was received", response.size());
        return new AdvertsInfoResponse().setAdverts(response);
    }

    @Override
    public AdvertStatisticResponse getAdvertStatistic(String advertId, String apiToken) {
        throw new UnsupportedOperationException("Get advert statistic method is not supported!");
    }

    @Override
    public void pauseAdvert(int advertId, String apiToken) {
        logger.info("Pause advert with id = {}", advertId);
        HttpHeaders headers = buildHttpHeadersForJsonBody(apiToken);
        HttpEntity<String> httpEntity = new HttpEntity<>("some", headers);

        Map<String, String> queryParams = Map.of(ADVERT_QUERY_ID_PARAM, String.valueOf(advertId));
        try {
            restTemplate.exchange(
                    RequestUtils.buildUri(
                            WildberriesApiMethods.PAUSE_ADVERT.getUrl()
                                    + WildberriesApiMethods.PAUSE_ADVERT.getMethod(),
                            queryParams),
                    HttpMethod.GET,
                    httpEntity,
                    String.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.PAUSE_ADVERT, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }

    @Override
    public void changeAdvertCpm(AdvertChangeCpmRequest request, String apiToken) {
        logger.info("Change advert cpm by request {}", request);
        HttpHeaders headers = buildHttpHeadersForJsonBody(apiToken);
        HttpEntity<AdvertChangeCpmRequest> httpEntity = new HttpEntity<>(request, headers);

        try {
            restTemplate.exchange(
                    WildberriesApiMethods.CHANGE_ADVERT_CPM.getUrl()
                            + WildberriesApiMethods.CHANGE_ADVERT_CPM.getMethod(),
                    HttpMethod.POST,
                    httpEntity,
                    String.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.CHANGE_ADVERT_CPM, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }

    @Override
    public void renameAdvert(int advertId, String name, String apiToken) {
        logger.info("Rename advert with id = {} by name = {}", advertId, name);
        AdvertRenameRequest request =
                new AdvertRenameRequest().setAdvertId(advertId).setName(name);
        HttpHeaders headers = buildHttpHeadersForJsonBody(apiToken);
        HttpEntity<AdvertRenameRequest> httpEntity = new HttpEntity<>(request, headers);

        try {
            restTemplate.exchange(
                    WildberriesApiMethods.RENAME_ADVERT.getUrl() + WildberriesApiMethods.RENAME_ADVERT.getMethod(),
                    HttpMethod.POST,
                    httpEntity,
                    Object.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.RENAME_ADVERT, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }

    @Override
    public AdvertUploadPhotoResponse uploadPhoto(AdvertUploadPhotoRequest request, String apiToken) {
        // TODO: нужно реализовать
        return new AdvertUploadPhotoResponse();
    }

    @Override
    public void changeStocks(ChangeStocksRequest request, int warehouseId, String apiToken) {}

    @Override
    public AdvertFullStatisticResponse[] getAdvertsFullStatisticByDates(
            List<AdvertFullStatisticDatesDto> request, String apiToken) {
        logger.info("Get advert full statistic by request = {}", request);
        HttpHeaders headers = buildHttpHeadersForJsonBody(apiToken);
        HttpEntity<List<AdvertFullStatisticDatesDto>> httpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<AdvertFullStatisticResponse[]> response;
        try {
            response = restTemplate.postForEntity(
                    WildberriesApiMethods.GET_ADVERTS_FULL_STATISTIC.getUrl()
                            + WildberriesApiMethods.GET_ADVERTS_FULL_STATISTIC.getMethod(),
                    httpEntity,
                    AdvertFullStatisticResponse[].class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.GET_ADVERTS_FULL_STATISTIC, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        logger.info("Received full statistic responses by dates = [{}]", List.of(response.getBody()));
        return response.getBody();
    }

    @Override
    public AdvertFullStatisticResponse[] getAdvertsFullStatisticByInterval(
            List<AdvertFullStatisticIntervalDto> request, String apiToken) {
        return new AdvertFullStatisticResponse[0];
    }

    @Override
    public GetGoodsResponse getGoods(GetGoodsRequest request, String apiToken) {
        logger.info("Get goods by request = {}", request);

        HttpHeaders headers = buildHttpHeadersForJsonBody(apiToken);
        HttpEntity<GetGoodsRequest> httpEntity = new HttpEntity<>(headers);

        Map<String, String> queryParams = new HashMap<>();
        if (request.getFilterNmID() != null) {
            queryParams.put(FILTER_NM_ID_QUERY_PARAM, String.valueOf(request.getFilterNmID()));
        }
        ResponseEntity<GetGoodsResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    RequestUtils.buildUri(
                            WildberriesApiMethods.GET_GOODS.getUrl() + WildberriesApiMethods.GET_GOODS.getMethod(),
                            queryParams),
                    HttpMethod.GET,
                    httpEntity,
                    GetGoodsResponse.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.GET_GOODS, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        logger.info("Set goods new price and discount response was received = [{}]", responseEntity);
        return responseEntity.getBody();
    }

    @Override
    public void setGoodPriceAndDiscount(SetGoodPriceRequest request, String apiToken) {
        logger.info("Set goods new price and discount by request = {}", request);

        HttpHeaders headers = buildHttpHeadersForJsonBody(apiToken);
        HttpEntity<SetGoodPriceRequest> httpEntity = new HttpEntity<>(request, headers);

        try {
            restTemplate.exchange(
                    WildberriesApiMethods.SET_GOODS_PRICE_AND_DISCOUNT.getUrl()
                            + WildberriesApiMethods.SET_GOODS_PRICE_AND_DISCOUNT.getMethod(),
                    HttpMethod.POST,
                    httpEntity,
                    String.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.SET_GOODS_PRICE_AND_DISCOUNT, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }

    @Override
    public int createAdvert(AdvertCreateRequest request, String apiToken) {
        throw new UnsupportedOperationException("Creating advert is not supported!");
    }

    @Override
    public CardTypeResponse getTypes(String apiToken) {
        logger.info("Get card types");

        HttpHeaders headers = buildHttpHeadersInternal(apiToken);
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<CardTypeResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    WildberriesApiMethods.GET_CARD_TYPES.getUrl() + WildberriesApiMethods.GET_CARD_TYPES.getMethod(),
                    HttpMethod.GET,
                    httpEntity,
                    CardTypeResponse.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.GET_CARD_TYPES, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        return responseEntity.getBody();
    }

    private HttpHeaders buildHttpHeadersForJsonBody(String apiToken) {
        HttpHeaders headers = buildHttpHeadersInternal(apiToken);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    private HttpHeaders buildHttpHeadersInternal(String apiToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, encryptionService.decrypt(apiToken));
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    @Getter
    @AllArgsConstructor
    private enum WildberriesApiMethods {
        GET_CARD_LIST("http://localhost:8083/api/cards/", "/content/v2/get/cards/list"),
        GET_CARD_TYPES("http://localhost:8083/api/cards/types", "/content/v2/directory/kinds"),
        GET_ADVERTS("http://localhost:8083/api/adverts", "/adv/v1/promotion/count"),
        START_ADVERT("http://localhost:8083/api/adverts", "/adv/v0/start"),
        RENAME_ADVERT("http://localhost:8083/api/adverts", "/adv/v0/rename"),
        PAUSE_ADVERT("http://localhost:8083/api/adverts", "/adv/v0/pause"),
        CHANGE_ADVERT_CPM("http://localhost:8083/api/adverts", "/adv/v0/cpm"),
        GET_ADVERTS_INFO("http://localhost:8083/api/adverts", "/adv/v1/promotion/adverts"),
        ADVERT_BUDGET_DEPOSIT("http://localhost:8083/api/adverts", "/adv/v1/budget/deposit"),
        ADVERT_BUDGET_INFO("http://localhost:8083/api/adverts", "/adv/v1/budget"),
        GET_GOODS("http://localhost:8083/api/cards", "/list/goods/filter"),
        SET_GOODS_PRICE_AND_DISCOUNT("http://localhost:8083/api/cards", "/upload/task"),
        GET_ADVERTS_FULL_STATISTIC("http://localhost:8083/api/adverts/statistics", "/v2/fullstats");

        private final String url;
        private final String method;
    }
}
