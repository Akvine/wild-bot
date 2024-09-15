package ru.akvine.wild.bot.services.integration.wildberries;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.*;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.*;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.type.CardTypeResponse;
import ru.akvine.wild.bot.utils.RequestUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!local")
public class WildberriesIntegrationServiceOrigin implements WildberriesIntegrationService {
    @Value("${wildberries.api.token}")
    private String apiToken;

    private final RestTemplate restTemplate = new RestTemplate();

    private final static int BUDGET_TYPE = 0;
    private final static String ADVERT_QUERY_ID_PARAM = "id";
    private final static String LIMIT_QUERY_PARAM = "limit";
    private final static String FILTER_NM_ID_QUERY_PARAM = "filterNmID";
    private final static int CARD_GET_LIMIT_COUNT = 100;

    @Override
    public List<CardDto> getCards() {
        logger.info("Get cards");
        FilterDto filter = new FilterDto().setWithPhoto(-1);
        CursorDto cursor = new CursorDto().setLimit(CARD_GET_LIMIT_COUNT);
        SettingsDto settings = new SettingsDto().setCursor(cursor).setFilter(filter);
        CardListRequest request = new CardListRequest().setSettings(settings);

        HttpHeaders headers = buildHttpHeadersForJsonBody();
        HttpEntity<CardListRequest> httpEntity = new HttpEntity<>(request, headers);

        List<CardDto> responseCards = new ArrayList<>();
        int size;
        do {
            ResponseEntity<CardListResponse> responseEntity;
            try {
                responseEntity = restTemplate.postForEntity(
                        WildberriesApiMethods.GET_CARD_LIST.getUrl() + WildberriesApiMethods.GET_CARD_LIST.getMethod(),
                        httpEntity,
                        CardListResponse.class
                );
            } catch (Exception exception) {
                String errorMessage = String.format(
                        "Error while calling wb api method = [%s]. Message = [%s]",
                        WildberriesApiMethods.GET_CARD_LIST, exception.getMessage());
                throw new IntegrationException(errorMessage);
            }
            List<CardDto> cards = Objects.requireNonNull(responseEntity.getBody()).getCards();
            size = cards.size();
            responseCards.addAll(cards);
            if (size >= CARD_GET_LIMIT_COUNT) {
                request.setSettings(settings.setCursor(getCursorWithLastUpdateAtAndNmId(cards)));
            }
        } while (size >= CARD_GET_LIMIT_COUNT);

        return responseCards;
    }

    private CursorDto getCursorWithLastUpdateAtAndNmId(List<CardDto> cards) {
        CardDto lastCard = cards.getLast();
        return new CursorDto()
                .setLimit(CARD_GET_LIMIT_COUNT)
                .setUpdatedAt(lastCard.getUpdatedAt())
                .setNmID(lastCard.getNmID());
    }

    @Override
    @RateLimiter(name = "wb-get-averts")
    public AdvertListResponse getAdverts() {
        logger.info("Get adverts list");
        HttpHeaders headers = buildHttpHeadersForJsonBody();
        HttpEntity<String> httpEntity = new HttpEntity<>("some", headers);

        ResponseEntity<AdvertListResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    WildberriesApiMethods.GET_ADVERTS.getUrl() + WildberriesApiMethods.GET_ADVERTS.getMethod(),
                    HttpMethod.GET,
                    httpEntity,
                    AdvertListResponse.class
            );
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
    @RateLimiter(name = "wb-advert-budget-info")
    public AdvertBudgetInfoResponse getAdvertBudgetInfo(int advertId) {
        logger.info("Get budget info for advert with id = {}", advertId);
        HttpHeaders headers = buildHttpHeadersForJsonBody();
        HttpEntity<String> httpEntity = new HttpEntity<>("some", headers);

        Map<String, String> queryParams = Map.of(ADVERT_QUERY_ID_PARAM, String.valueOf(advertId));
        ResponseEntity<AdvertBudgetInfoResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    RequestUtils.buildUri(WildberriesApiMethods.ADVERT_BUDGET_INFO.getUrl() + WildberriesApiMethods.ADVERT_BUDGET_INFO.getMethod(), queryParams),
                    HttpMethod.GET,
                    httpEntity,
                    AdvertBudgetInfoResponse.class
            );
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
    @RateLimiter(name = "wb-advert-budget-deposit")
    public AdvertBudgetDepositResponse advertBudgetDeposit(int advertId, int sum) {
        logger.info("Deposit budget advert with id = {} and sum = {}", advertId, sum);
        AdvertBudgetDepositRequest request = new AdvertBudgetDepositRequest()
                .setReturn(true)
                .setType(BUDGET_TYPE)
                .setSum(sum);

        HttpHeaders headers = buildHttpHeadersForJsonBody();
        HttpEntity<AdvertBudgetDepositRequest> httpEntity = new HttpEntity<>(request, headers);

        Map<String, String> queryParams = Map.of(ADVERT_QUERY_ID_PARAM, String.valueOf(advertId));
        ResponseEntity<AdvertBudgetDepositResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    RequestUtils.buildUri(WildberriesApiMethods.ADVERT_BUDGET_DEPOSIT.getUrl() + WildberriesApiMethods.ADVERT_BUDGET_DEPOSIT.getMethod(), queryParams),
                    HttpMethod.POST,
                    httpEntity,
                    AdvertBudgetDepositResponse.class
            );
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
    @RateLimiter(name = "wb-advert-start")
    public void startAdvert(int advertId) {
        logger.info("Start advert with id = {}", advertId);
        HttpHeaders headers = buildHttpHeadersForJsonBody();
        HttpEntity<String> httpEntity = new HttpEntity<>("some", headers);

        Map<String, String> queryParams = Map.of(ADVERT_QUERY_ID_PARAM, String.valueOf(advertId));
        try {
            restTemplate.exchange(
                    RequestUtils.buildUri(WildberriesApiMethods.START_ADVERT.getUrl() + WildberriesApiMethods.START_ADVERT.getMethod(), queryParams),
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.START_ADVERT, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }

    @Override
    @RateLimiter(name = "wb-get-adverts-info")
    public AdvertsInfoResponse getAdvertsInfo(List<Integer> advertIds) {
        logger.info("Get adverts info");
        HttpHeaders headers = buildHttpHeadersForJsonBody();
        HttpEntity<List<Integer>> httpEntity = new HttpEntity<>(advertIds, headers);

        ResponseEntity<AdvertDto[]> responseEntity;
        try {
            responseEntity = restTemplate.postForEntity(
                    WildberriesApiMethods.GET_ADVERTS_INFO.getUrl() + WildberriesApiMethods.GET_ADVERTS_INFO.getMethod(),
                    httpEntity,
                    AdvertDto[].class
            );
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.GET_ADVERTS_INFO, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        List<AdvertDto> response = List.of(Objects.requireNonNull(responseEntity.getBody()));
        logger.info("Get adverts info response with size = {} was received", response.size());
        return new AdvertsInfoResponse().setAdverts(response);
    }

    @Override
    @RateLimiter(name = "wb-get-advert-statistic")
    public AdvertStatisticResponse getAdvertStatistic(String advertId) {
        logger.info("Get statistic fo advert with id = {}", advertId);
        HttpHeaders headers = buildHttpHeadersForJsonBody();
        HttpEntity<String> httpEntity = new HttpEntity<>("some", headers);

        Map<String, String> queryParams = Map.of(ADVERT_QUERY_ID_PARAM, advertId);
        ResponseEntity<AdvertStatisticResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    RequestUtils.buildUri(WildberriesApiMethods.GET_ADVERT_STATISTIC.getUrl() + WildberriesApiMethods.GET_ADVERT_STATISTIC.getMethod(), queryParams),
                    HttpMethod.GET,
                    httpEntity,
                    AdvertStatisticResponse.class
            );
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.GET_ADVERT_STATISTIC, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        AdvertStatisticResponse response = responseEntity.getBody();
        logger.info("Get advert statistic response = [{}]", response);
        return response;
    }

    @Override
    @RateLimiter(name = "wb-advert-pause")
    public void pauseAdvert(int advertId) {
        logger.info("Pause advert with id = {}", advertId);
        HttpHeaders headers = buildHttpHeadersForJsonBody();
        HttpEntity<String> httpEntity = new HttpEntity<>("some", headers);

        Map<String, String> queryParams = Map.of(ADVERT_QUERY_ID_PARAM, String.valueOf(advertId));
        try {
            restTemplate.exchange(
                    RequestUtils.buildUri(WildberriesApiMethods.PAUSE_ADVERT.getUrl() + WildberriesApiMethods.PAUSE_ADVERT.getMethod(), queryParams),
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.PAUSE_ADVERT, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }

    @Override
    @RateLimiter(name = "wb-change-advert-cpm")
    public void changeAdvertCpm(AdvertChangeCpmRequest request) {
        logger.info("Change advert cpm by request {}", request);
        HttpHeaders headers = buildHttpHeadersForJsonBody();
        HttpEntity<AdvertChangeCpmRequest> httpEntity = new HttpEntity<>(request, headers);

        try {
            restTemplate.exchange(
                    WildberriesApiMethods.CHANGE_ADVERT_CPM.getUrl() + WildberriesApiMethods.CHANGE_ADVERT_CPM.getMethod(),
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.CHANGE_ADVERT_CPM, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }

    @Override
    public void renameAdvert(int advertId, String name) {
        logger.info("Rename advert with id = {} by name = {}", advertId, name);
        AdvertRenameRequest request = new AdvertRenameRequest()
                .setAdvertId(advertId)
                .setName(name);
        HttpHeaders headers = buildHttpHeadersForJsonBody();
        HttpEntity<AdvertRenameRequest> httpEntity = new HttpEntity<>(request, headers);

        try {
            restTemplate.exchange(
                    WildberriesApiMethods.RENAME_ADVERT.getUrl() + WildberriesApiMethods.RENAME_ADVERT.getMethod(),
                    HttpMethod.POST,
                    httpEntity,
                    Object.class
            );
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.RENAME_ADVERT, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }

    @Override
    public AdvertUploadPhotoResponse uploadPhoto(AdvertUploadPhotoRequest request) {
        logger.info("Upload photo with number = {} for card with id = {}", request.getPhotoNumber(), request.getNmId());

        Map<String, String> fields = Map.of(
                "X-Nm-Id", String.valueOf(request.getNmId()),
                "X-Photo-Number", String.valueOf(request.getPhotoNumber())
        );
        HttpHeaders headers = buildHttpHeadersForMultipartBody(fields);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("uploadfile", request.getUploadFile());
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<AdvertUploadPhotoResponse> response;
        try {
            response = restTemplate.postForEntity(
                    WildberriesApiMethods.UPLOAD_CARD_PHOTO.getUrl() + WildberriesApiMethods.UPLOAD_CARD_PHOTO.getMethod(),
                    requestEntity,
                    AdvertUploadPhotoResponse.class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.UPLOAD_CARD_PHOTO, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        AdvertUploadPhotoResponse bodyResponse = response.getBody();
        logger.info("Advert upload photo response was received = [{}]", bodyResponse);
        return bodyResponse;
    }

    @Override
    public void changeStocks(ChangeStocksRequest request, int warehouseId) {
        logger.info("Change stocks by request {} for warehouse with id = {}", request, warehouseId);
        HttpHeaders headers = buildHttpHeadersForJsonBody();
        HttpEntity<ChangeStocksRequest> httpEntity = new HttpEntity<>(request, headers);

        try {
            restTemplate.exchange(
                    WildberriesApiMethods.CHANGE_CARD_STOCKS.getUrl() + WildberriesApiMethods.CHANGE_CARD_STOCKS.getMethod() + warehouseId,
                    HttpMethod.PUT,
                    httpEntity,
                    String.class
            );
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.CHANGE_CARD_STOCKS, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }

    @Override
    @RateLimiter(name = "wb-get-adverts-full-statistic-by-dates")
    public AdvertFullStatisticResponse[] getAdvertsFullStatisticByDates(List<AdvertFullStatisticDatesDto> request) {
        logger.info("Get advert full statistic by request = {}", request);
        HttpHeaders headers = buildHttpHeadersForJsonBody();
        HttpEntity<List<AdvertFullStatisticDatesDto>> httpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<AdvertFullStatisticResponse[]> response;
        try {
            response = restTemplate.postForEntity(
                    WildberriesApiMethods.GET_ADVERTS_FULL_STATISTIC.getUrl() + WildberriesApiMethods.GET_ADVERTS_FULL_STATISTIC.getMethod(),
                    httpEntity,
                    AdvertFullStatisticResponse[].class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.GET_ADVERTS_FULL_STATISTIC, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        // TODO: вынести валидацию из сервиса. Данный класс выступает как просто клиент для отправки запросов / получения ответов
        AdvertFullStatisticResponse[] responses = response.getBody();
        if (responses == null || responses.length == 0) {
            throw new IntegrationException("Full statistic responses by dates is null or empty");
        }

        logger.info("Received full statistic responses by dates = [{}]", List.of(responses));
        return responses;
    }

    @Override
    @RateLimiter(name = "wb-get-adverts-full-statistic-by-interval")
    public AdvertFullStatisticResponse[] getAdvertsFullStatisticByInterval(List<AdvertFullStatisticIntervalDto> request) {
        logger.info("Get advert full statistic interval by request = {}", request);
        HttpHeaders headers = buildHttpHeadersForJsonBody();
        HttpEntity<List<AdvertFullStatisticIntervalDto>> httpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<AdvertFullStatisticResponse[]> response;
        try {
            response = restTemplate.postForEntity(
                    WildberriesApiMethods.GET_ADVERTS_FULL_STATISTIC.getUrl() + WildberriesApiMethods.GET_ADVERTS_FULL_STATISTIC.getMethod(),
                    httpEntity,
                    AdvertFullStatisticResponse[].class);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.GET_ADVERTS_FULL_STATISTIC, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        // TODO: вынести валидацию из сервиса. Данный класс выступает как просто клиент для отправки запросов / получения ответов
        AdvertFullStatisticResponse[] responses = response.getBody();
        if (responses == null || responses.length == 0) {
            throw new IntegrationException("Full statistic responses by interval is null or empty");
        }

        logger.info("Received full statistic responses by interval = [{}]", List.of(responses));
        return responses;
    }

    @Override
    public GetGoodsResponse getGoods(GetGoodsRequest request) {
        logger.info("Get goods by request = {}", request);

        HttpHeaders headers = buildHttpHeadersForJsonBody();
        HttpEntity<GetGoodsRequest> httpEntity = new HttpEntity<>(headers);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(LIMIT_QUERY_PARAM, String.valueOf(request.getLimit()));
        if (request.getFilterNmID() != null) {
            queryParams.put(FILTER_NM_ID_QUERY_PARAM, String.valueOf(request.getFilterNmID()));
        }

        ResponseEntity<GetGoodsResponse> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    RequestUtils.buildUri(WildberriesApiMethods.GET_GOODS.getUrl() + WildberriesApiMethods.GET_GOODS.getMethod(), queryParams),
                    HttpMethod.GET,
                    httpEntity,
                    GetGoodsResponse.class
            );
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.GET_GOODS, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        // TODO: вынести валидацию из сервиса. Данный класс выступает как просто клиент для отправки запросов / получения ответов
        GetGoodsResponse response = responseEntity.getBody();
        if (response == null) {
            throw new IntegrationException("Get goods response is null");
        }

        logger.info("Set goods new price and discount response was received = [{}]", response);
        return response;
    }

    @Override
    public void setGoodPriceAndDiscount(SetGoodPriceRequest request) {
        logger.info("Set goods new price and discount by request = {}", request);

        HttpHeaders headers = buildHttpHeadersForJsonBody();
        HttpEntity<SetGoodPriceRequest> httpEntity = new HttpEntity<>(request, headers);

        try {
            restTemplate.exchange(
                    WildberriesApiMethods.SET_GOODS_PRICE_AND_DISCOUNT.getUrl() + WildberriesApiMethods.SET_GOODS_PRICE_AND_DISCOUNT.getMethod(),
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.SET_GOODS_PRICE_AND_DISCOUNT, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }

    @Override
    public int createAdvert(AdvertCreateRequest request) {
        logger.info("Create advert by request = {}", request);

        HttpHeaders headers = buildHttpHeadersForJsonBody();
        HttpEntity<AdvertCreateRequest> httpEntity = new HttpEntity<>(request, headers);

        String response;
        try {
            response = restTemplate.exchange(
                    WildberriesApiMethods.CREATE_AUTO_ADVERT.getUrl() + WildberriesApiMethods.CREATE_AUTO_ADVERT.getMethod(),
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            ).getBody();
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling wb api method = [%s]. Message = [%s]",
                    WildberriesApiMethods.CREATE_AUTO_ADVERT, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }

        return Integer.parseInt(response);
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

    private HttpHeaders buildHttpHeadersForJsonBody() {
        HttpHeaders headers = buildHttpHeadersInternal();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    private HttpHeaders buildHttpHeadersForMultipartBody(Map<String, String> fieldsNamesWithValues) {
        HttpHeaders headers = buildHttpHeadersForJsonBody();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        fieldsNamesWithValues.forEach(headers::add);
        return headers;
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
        GET_CARD_LIST("https://suppliers-api.wildberries.ru","/content/v2/get/cards/list?locale=ru"),
        GET_ADVERTS("https://advert-api.wb.ru", "/adv/v1/promotion/count"),
        ADVERT_BUDGET_DEPOSIT("https://advert-api.wb.ru","/adv/v1/budget/deposit"),
        START_ADVERT("https://advert-api.wb.ru", "/adv/v0/start"),
        PAUSE_ADVERT("https://advert-api.wb.ru","/adv/v0/pause"),
        CHANGE_ADVERT_CPM("https://advert-api.wb.ru", "/adv/v0/cpm"),
        RENAME_ADVERT("https://advert-api.wb.ru", "/adv/v0/rename"),
        GET_ADVERTS_INFO( "https://advert-api.wb.ru", "/adv/v1/promotion/adverts"),
        GET_ADVERT_STATISTIC("https://advert-api.wb.ru", "/adv/v1/auto/stat" ),
        UPLOAD_CARD_PHOTO("https://suppliers-api.wildberries.ru","/content/v3/media/file"),
        ADVERT_BUDGET_INFO("https://advert-api.wb.ru","/adv/v1/budget"),
        CHANGE_CARD_STOCKS("https://marketplace-api.wildberries.ru", "/api/v3/stocks/"),
        GET_ADVERTS_FULL_STATISTIC("https://advert-api.wb.ru", "/adv/v2/fullstats"),
        GET_GOODS("https://discounts-prices-api.wildberries.ru", "/api/v2/list/goods/filter"),
        SET_GOODS_PRICE_AND_DISCOUNT("https://discounts-prices-api.wildberries.ru", "/api/v2/upload/task"),
        CREATE_AUTO_ADVERT("https://advert-api.wildberries.ru", "/adv/v1/save-ad"),
        GET_CARD_TYPES("https://content-api.wildberries.ru", "/content/v2/directory/kinds");

        private final String url;
        private final String method;
    }
}
