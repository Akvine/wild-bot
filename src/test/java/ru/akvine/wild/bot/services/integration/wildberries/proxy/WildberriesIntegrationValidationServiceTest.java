package ru.akvine.wild.bot.services.integration.wildberries.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.akvine.wild.bot.enums.ProxyType;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertBudgetDepositResponse;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertBudgetInfoResponse;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertChangeCpmRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertCreateRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertFullStatisticResponse;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertListResponse;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertStatisticResponse;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertUploadPhotoRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertUploadPhotoResponse;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertsInfoResponse;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.GetGoodsRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.GetGoodsResponse;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.SetGoodPriceRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.CardDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.ChangeStocksRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.type.CardTypeResponse;

@DisplayName("WildberriesIntegrationValidationService")
class WildberriesIntegrationValidationServiceTest {
    private static final String TOKEN = "token";

    private WildberriesIntegrationService target;
    private WildberriesIntegrationValidationService service;

    @BeforeEach
    void setUp() {
        target = mock(WildberriesIntegrationService.class);
        service = new WildberriesIntegrationValidationService();
        service.setTargetObject(target);
    }

    @Test
    @DisplayName("Тип прокси - VALIDATION")
    void type() {
        assertThat(service.getType()).isEqualTo(ProxyType.VALIDATION);
    }

    @Test
    @DisplayName("Пустой токен отклоняется до обращения к WB для любого метода")
    void blankTokenIsRejectedEverywhere() {
        List<Runnable> calls = List.of(
                () -> service.getCards(" "),
                () -> service.getAdverts(null),
                () -> service.getAdvertBudgetInfo(1, ""),
                () -> service.advertBudgetDeposit(1, 1, ""),
                () -> service.startAdvert(1, ""),
                () -> service.getAdvertsInfo(List.of(), ""),
                () -> service.getAdvertStatistic("1", ""),
                () -> service.pauseAdvert(1, ""),
                () -> service.changeAdvertCpm(new AdvertChangeCpmRequest(), ""),
                () -> service.renameAdvert(1, "n", ""),
                () -> service.uploadPhoto(new AdvertUploadPhotoRequest(), ""),
                () -> service.changeStocks(new ChangeStocksRequest(), 1, ""),
                () -> service.getAdvertsFullStatisticByDates(List.of(), ""),
                () -> service.getAdvertsFullStatisticByInterval(List.of(), ""),
                () -> service.getGoods(new GetGoodsRequest(), ""),
                () -> service.setGoodPriceAndDiscount(new SetGoodPriceRequest(), ""),
                () -> service.createAdvert(new AdvertCreateRequest(), ""),
                () -> service.getTypes(""));

        for (Runnable call : calls) {
            assertThatThrownBy(call::run)
                    .isInstanceOf(IntegrationException.class)
                    .hasMessage("apiToken is null");
        }
    }

    @Test
    @DisplayName("С токеном вызовы проходят в исходный сервис и возвращают его ответ")
    void validCallsAreDelegated() {
        List<CardDto> cards = List.of(new CardDto());
        AdvertListResponse adverts = new AdvertListResponse();
        AdvertBudgetInfoResponse budget = new AdvertBudgetInfoResponse();
        AdvertBudgetDepositResponse deposit = new AdvertBudgetDepositResponse();
        AdvertsInfoResponse info = new AdvertsInfoResponse();
        AdvertStatisticResponse statistic = new AdvertStatisticResponse();
        AdvertUploadPhotoResponse photo = new AdvertUploadPhotoResponse();
        AdvertUploadPhotoRequest photoRequest = new AdvertUploadPhotoRequest();
        when(target.getCards(TOKEN)).thenReturn(cards);
        when(target.getAdverts(TOKEN)).thenReturn(adverts);
        when(target.getAdvertBudgetInfo(1, TOKEN)).thenReturn(budget);
        when(target.advertBudgetDeposit(1, 5, TOKEN)).thenReturn(deposit);
        when(target.getAdvertsInfo(List.of(1), TOKEN)).thenReturn(info);
        when(target.getAdvertStatistic("1", TOKEN)).thenReturn(statistic);
        when(target.uploadPhoto(photoRequest, TOKEN)).thenReturn(photo);
        AdvertCreateRequest createRequest = new AdvertCreateRequest();
        when(target.createAdvert(createRequest, TOKEN)).thenReturn(42);

        assertThat(service.getCards(TOKEN)).isSameAs(cards);
        assertThat(service.getAdverts(TOKEN)).isSameAs(adverts);
        assertThat(service.getAdvertBudgetInfo(1, TOKEN)).isSameAs(budget);
        assertThat(service.advertBudgetDeposit(1, 5, TOKEN)).isSameAs(deposit);
        assertThat(service.getAdvertsInfo(List.of(1), TOKEN)).isSameAs(info);
        assertThat(service.getAdvertStatistic("1", TOKEN)).isSameAs(statistic);
        assertThat(service.uploadPhoto(photoRequest, TOKEN)).isSameAs(photo);
        assertThat(service.createAdvert(createRequest, TOKEN)).isEqualTo(42);

        AdvertChangeCpmRequest cpm = new AdvertChangeCpmRequest();
        ChangeStocksRequest stocks = new ChangeStocksRequest();
        SetGoodPriceRequest price = new SetGoodPriceRequest();
        service.startAdvert(1, TOKEN);
        service.pauseAdvert(1, TOKEN);
        service.changeAdvertCpm(cpm, TOKEN);
        service.renameAdvert(1, "name", TOKEN);
        service.changeStocks(stocks, 7, TOKEN);
        service.setGoodPriceAndDiscount(price, TOKEN);

        verify(target).startAdvert(1, TOKEN);
        verify(target).pauseAdvert(1, TOKEN);
        verify(target).changeAdvertCpm(cpm, TOKEN);
        verify(target).renameAdvert(1, "name", TOKEN);
        verify(target).changeStocks(stocks, 7, TOKEN);
        verify(target).setGoodPriceAndDiscount(price, TOKEN);
    }

    @Test
    @DisplayName("Пустая полная статистика (по датам и интервалу) - ошибка интеграции")
    void emptyFullStatisticIsAnError() {
        when(target.getAdvertsFullStatisticByDates(List.of(), TOKEN)).thenReturn(new AdvertFullStatisticResponse[0]);
        when(target.getAdvertsFullStatisticByInterval(List.of(), TOKEN)).thenReturn(null);

        assertThatThrownBy(() -> service.getAdvertsFullStatisticByDates(List.of(), TOKEN))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("by dates");
        assertThatThrownBy(() -> service.getAdvertsFullStatisticByInterval(List.of(), TOKEN))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("by interval");
    }

    @Test
    @DisplayName("Непустая полная статистика возвращается как есть")
    void fullStatisticIsReturned() {
        AdvertFullStatisticResponse[] responses = {new AdvertFullStatisticResponse()};
        when(target.getAdvertsFullStatisticByDates(List.of(), TOKEN)).thenReturn(responses);
        when(target.getAdvertsFullStatisticByInterval(List.of(), TOKEN)).thenReturn(responses);

        assertThat(service.getAdvertsFullStatisticByDates(List.of(), TOKEN)).isSameAs(responses);
        assertThat(service.getAdvertsFullStatisticByInterval(List.of(), TOKEN)).isSameAs(responses);
    }

    @Test
    @DisplayName("getGoods: пустой ответ - ошибка; getTypes: признак ошибки в ответе - ошибка")
    void goodsAndTypesResponseValidation() {
        GetGoodsRequest request = new GetGoodsRequest();
        assertThatThrownBy(() -> service.getGoods(request, TOKEN))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("null");
        GetGoodsResponse goods = new GetGoodsResponse();
        when(target.getGoods(request, TOKEN)).thenReturn(goods);
        assertThat(service.getGoods(request, TOKEN)).isSameAs(goods);

        CardTypeResponse failed = new CardTypeResponse().setError("true").setErrorText("boom");
        when(target.getTypes("bad")).thenReturn(failed);
        assertThatThrownBy(() -> service.getTypes("bad"))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("boom");

        CardTypeResponse ok = new CardTypeResponse().setError("false");
        when(target.getTypes(TOKEN)).thenReturn(ok);
        assertThat(service.getTypes(TOKEN)).isSameAs(ok);
    }
}
