package ru.akvine.wild.bot.services.integration.wildberries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.infrastructure.resilience.BulkheadFactory;
import ru.akvine.wild.bot.infrastructure.resilience.CircuitBreakerInterceptorFactory;
import ru.akvine.wild.bot.services.encryption.EncryptionService;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertChangeCpmRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertCreateRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertFullStatisticDatesDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertFullStatisticIntervalDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertUploadPhotoRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.GetGoodsRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.SetGoodDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.SetGoodPriceRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.ChangeStocksRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.SkuDto;

@DisplayName("WildberriesIntegrationServiceOrigin")
class WildberriesIntegrationServiceOriginTest {
    private static final String TOKEN = "encrypted";
    private static final String ADVERT_API = "https://advert-api.wb.ru";

    private WildberriesIntegrationServiceOrigin service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        EncryptionService encryption = mock(EncryptionService.class);
        when(encryption.decrypt(TOKEN)).thenReturn("decrypted-token");
        CircuitBreakerInterceptorFactory circuitBreakers = mock(CircuitBreakerInterceptorFactory.class);
        BulkheadFactory bulkheads = mock(BulkheadFactory.class);
        when(bulkheads.interceptor("wildberries"))
                .thenReturn((request, body, execution) -> execution.execute(request, body));
        when(circuitBreakers.create("wildberries"))
                .thenReturn((request, body, execution) -> execution.execute(request, body));
        service = new WildberriesIntegrationServiceOrigin(encryption, circuitBreakers, bulkheads);
        service.initResilience();
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        server = MockRestServiceServer.bindTo(restTemplate).build();
    }

    private static String cards(int count, int firstId) {
        return IntStream.range(0, count)
                .mapToObj(i -> "{\"nmID\":" + (firstId + i) + ",\"title\":\"t" + i + "\",\"updatedAt\":\"2024-01-0"
                        + (1 + i % 9) + "T00:00:00Z\"}")
                .collect(Collectors.joining(",", "{\"cards\":[", "]}"));
    }

    @Test
    @DisplayName("getCards: идёт по страницам курсором, пока страница полная")
    void getCardsPaginatesWithCursor() {
        server.expect(requestTo("https://suppliers-api.wildberries.ru/content/v2/get/cards/list?locale=ru"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "decrypted-token"))
                .andExpect(jsonPath("$.settings.cursor.limit").value(100))
                .andRespond(withSuccess(cards(100, 1), MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://suppliers-api.wildberries.ru/content/v2/get/cards/list?locale=ru"))
                .andExpect(jsonPath("$.settings.cursor.nmID").value(100))
                .andRespond(withSuccess(cards(3, 101), MediaType.APPLICATION_JSON));

        assertThat(service.getCards(TOKEN)).hasSize(103);
        server.verify();
    }

    @Test
    @DisplayName("getCards: ошибка WB превращается в IntegrationException с названием метода")
    void getCardsFailure() {
        server.expect(requestTo("https://suppliers-api.wildberries.ru/content/v2/get/cards/list?locale=ru"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> service.getCards(TOKEN))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("GET_CARD_LIST");
    }

    @Test
    @DisplayName(
            "getAdverts, getAdvertBudgetInfo, advertBudgetDeposit, getAdvertStatistic возвращают разобранные ответы")
    void advertQueries() {
        server.expect(requestTo(ADVERT_API + "/adv/v1/promotion/count"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"adverts\":[],\"all\":3}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(ADVERT_API + "/adv/v1/budget?id=5"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"total\":777}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(ADVERT_API + "/adv/v1/budget/deposit?id=5"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.sum").value(100))
                .andRespond(withSuccess("{\"total\":10}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(ADVERT_API + "/adv/v1/auto/stat?id=5"))
                .andRespond(withSuccess("{\"views\":\"10\",\"clicks\":\"2\"}", MediaType.APPLICATION_JSON));

        assertThat(service.getAdverts(TOKEN).getAll()).isEqualTo(3);
        assertThat(service.getAdvertBudgetInfo(5, TOKEN).getTotal()).isEqualTo(777);
        assertThat(service.advertBudgetDeposit(5, 100, TOKEN).getTotal()).isEqualTo(10);
        assertThat(service.getAdvertStatistic("5", TOKEN).getViews()).isEqualTo("10");
        server.verify();
    }

    @Test
    @DisplayName(
            "startAdvert, pauseAdvert, changeAdvertCpm, renameAdvert, changeStocks, setGoodPriceAndDiscount шлют нужные запросы")
    void advertCommands() {
        server.expect(requestTo(ADVERT_API + "/adv/v0/start?id=5"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));
        server.expect(requestTo(ADVERT_API + "/adv/v0/pause?id=5"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));
        server.expect(requestTo(ADVERT_API + "/adv/v0/cpm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.cpm").value(250))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));
        server.expect(requestTo(ADVERT_API + "/adv/v0/rename"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.name").value("new name"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://marketplace-api.wildberries.ru/api/v3/stocks/77"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(jsonPath("$.stocks[0].sku").value("barcode"))
                .andRespond(withSuccess("", MediaType.TEXT_PLAIN));
        server.expect(requestTo("https://discounts-prices-api.wildberries.ru/api/v2/upload/task"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.data[0].price").value(999))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        service.startAdvert(5, TOKEN);
        service.pauseAdvert(5, TOKEN);
        service.changeAdvertCpm(new AdvertChangeCpmRequest().setAdvertId(5).setCpm(250), TOKEN);
        service.renameAdvert(5, "new name", TOKEN);
        service.changeStocks(
                new ChangeStocksRequest()
                        .setStocks(List.of(new SkuDto().setSku("barcode").setAmount(0))),
                77,
                TOKEN);
        service.setGoodPriceAndDiscount(
                new SetGoodPriceRequest()
                        .setData(List.of(
                                new SetGoodDto().setNmID(1).setPrice(999).setDiscount(10))),
                TOKEN);
        server.verify();
    }

    @Test
    @DisplayName("getAdvertsInfo и полная статистика по датам и интервалу")
    void advertInfoAndFullStatistics() {
        server.expect(requestTo(ADVERT_API + "/adv/v1/promotion/adverts"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("[1,2]"))
                .andRespond(withSuccess("[{\"advertId\":1},{\"advertId\":2}]", MediaType.APPLICATION_JSON));
        server.expect(requestTo(ADVERT_API + "/adv/v2/fullstats"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("[{\"advertId\":\"1\",\"views\":\"9\"}]", MediaType.APPLICATION_JSON));
        server.expect(requestTo(ADVERT_API + "/adv/v2/fullstats"))
                .andRespond(withSuccess("[{\"advertId\":\"2\",\"views\":\"7\"}]", MediaType.APPLICATION_JSON));

        assertThat(service.getAdvertsInfo(List.of(1, 2), TOKEN).getAdverts()).hasSize(2);
        assertThat(service
                        .getAdvertsFullStatisticByDates(
                                List.of(new AdvertFullStatisticDatesDto()
                                        .setId(1)
                                        .setDates(List.of("2024-01-01"))),
                                TOKEN)[0]
                        .getViews())
                .isEqualTo("9");
        assertThat(service
                        .getAdvertsFullStatisticByInterval(
                                List.of(new AdvertFullStatisticIntervalDto().setId(2)), TOKEN)[0]
                        .getViews())
                .isEqualTo("7");
        server.verify();
    }

    @Test
    @DisplayName("uploadPhoto шлёт multipart с заголовками X-Nm-Id и X-Photo-Number")
    void uploadPhoto() {
        server.expect(requestTo("https://suppliers-api.wildberries.ru/content/v3/media/file"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Nm-Id", "15"))
                .andExpect(header("X-Photo-Number", "2"))
                .andRespond(withSuccess("{\"error\":false,\"errorText\":\"\"}", MediaType.APPLICATION_JSON));

        var response = service.uploadPhoto(
                new AdvertUploadPhotoRequest().setNmId(15).setPhotoNumber(2).setUploadFile(new byte[] {1, 2, 3}),
                TOKEN);

        assertThat(response).isNotNull();
        server.verify();
    }

    @Test
    @DisplayName("getGoods передаёт limit и необязательный filterNmID")
    void getGoods() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(
                        "https://discounts-prices-api.wildberries.ru/api/v2/list/goods/filter")))
                .andExpect(queryParam("limit", "10"))
                .andExpect(queryParam("filterNmID", "5"))
                .andRespond(withSuccess(
                        "{\"data\":{\"listGoods\":[{\"nmId\":\"5\",\"discount\":7}]}}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(org.hamcrest.Matchers.startsWith(
                        "https://discounts-prices-api.wildberries.ru/api/v2/list/goods/filter")))
                .andExpect(queryParam("limit", "20"))
                .andRespond(withSuccess("{\"data\":{\"listGoods\":[]}}", MediaType.APPLICATION_JSON));

        assertThat(service.getGoods(new GetGoodsRequest().setLimit(10).setFilterNmID(5), TOKEN)
                        .getData()
                        .getListGoods())
                .hasSize(1);
        assertThat(service.getGoods(new GetGoodsRequest().setLimit(20), TOKEN)
                        .getData()
                        .getListGoods())
                .isEmpty();
        server.verify();
    }

    @Test
    @DisplayName("createAdvert возвращает id кампании из тела ответа")
    void createAdvert() {
        server.expect(requestTo("https://advert-api.wildberries.ru/adv/v1/save-ad"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.name").value("campaign"))
                .andRespond(withSuccess("12345", MediaType.TEXT_PLAIN));

        assertThat(service.createAdvert(new AdvertCreateRequest().setName("campaign"), TOKEN))
                .isEqualTo(12345);
    }

    @Test
    @DisplayName("getTypes читает справочник типов товаров")
    void getTypes() {
        server.expect(requestTo("https://content-api.wildberries.ru/content/v2/directory/kinds"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "decrypted-token"))
                .andRespond(withSuccess("{\"data\":[\"Женский\",\"Мужской\"]}", MediaType.APPLICATION_JSON));

        assertThat(service.getTypes(TOKEN).getData()).containsExactly("Женский", "Мужской");
    }

    @Test
    @DisplayName("Каждый метод оборачивает ошибку в IntegrationException")
    void allMethodsWrapFailures() {
        server.expect(
                        org.springframework.test.web.client.ExpectedCount.manyTimes(),
                        requestTo(org.hamcrest.Matchers.any(String.class)))
                .andRespond(withServerError());
        List<NamedCall> calls = List.of(
                new NamedCall("GET_ADVERTS", () -> service.getAdverts(TOKEN)),
                new NamedCall("ADVERT_BUDGET_INFO", () -> service.getAdvertBudgetInfo(1, TOKEN)),
                new NamedCall("ADVERT_BUDGET_DEPOSIT", () -> service.advertBudgetDeposit(1, 1, TOKEN)),
                new NamedCall("START_ADVERT", () -> service.startAdvert(1, TOKEN)),
                new NamedCall("GET_ADVERTS_INFO", () -> service.getAdvertsInfo(List.of(1), TOKEN)),
                new NamedCall("GET_ADVERT_STATISTIC", () -> service.getAdvertStatistic("1", TOKEN)),
                new NamedCall("PAUSE_ADVERT", () -> service.pauseAdvert(1, TOKEN)),
                new NamedCall("CHANGE_ADVERT_CPM", () -> service.changeAdvertCpm(new AdvertChangeCpmRequest(), TOKEN)),
                new NamedCall("RENAME_ADVERT", () -> service.renameAdvert(1, "n", TOKEN)),
                new NamedCall(
                        "UPLOAD_CARD_PHOTO",
                        () -> service.uploadPhoto(new AdvertUploadPhotoRequest().setUploadFile(new byte[0]), TOKEN)),
                new NamedCall("CHANGE_CARD_STOCKS", () -> service.changeStocks(new ChangeStocksRequest(), 1, TOKEN)),
                new NamedCall(
                        "GET_ADVERTS_FULL_STATISTIC", () -> service.getAdvertsFullStatisticByDates(List.of(), TOKEN)),
                new NamedCall(
                        "GET_ADVERTS_FULL_STATISTIC",
                        () -> service.getAdvertsFullStatisticByInterval(List.of(), TOKEN)),
                new NamedCall("GET_GOODS", () -> service.getGoods(new GetGoodsRequest().setLimit(1), TOKEN)),
                new NamedCall(
                        "SET_GOODS_PRICE_AND_DISCOUNT",
                        () -> service.setGoodPriceAndDiscount(new SetGoodPriceRequest(), TOKEN)),
                new NamedCall("CREATE_AUTO_ADVERT", () -> service.createAdvert(new AdvertCreateRequest(), TOKEN)),
                new NamedCall("GET_CARD_TYPES", () -> service.getTypes(TOKEN)));

        for (NamedCall call : calls) {
            assertThatThrownBy(call.action::run)
                    .as(call.apiMethod)
                    .isInstanceOf(IntegrationException.class)
                    .hasMessageContaining(call.apiMethod);
        }
    }

    private record NamedCall(String apiMethod, Runnable action) {}
}
