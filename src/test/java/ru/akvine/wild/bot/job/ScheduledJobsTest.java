package ru.akvine.wild.bot.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;
import ru.akvine.wild.bot.entities.AdvertEntity;
import ru.akvine.wild.bot.entities.CardEntity;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.entities.SubscriptionEntity;
import ru.akvine.wild.bot.enums.AdvertStatus;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.infrastructure.counter.CountersStorage;
import ru.akvine.wild.bot.job.domain.DeleteAdvertsAndStatisticsJob;
import ru.akvine.wild.bot.repositories.AdvertRepository;
import ru.akvine.wild.bot.repositories.AdvertStatisticRepository;
import ru.akvine.wild.bot.repositories.SubscriptionRepository;
import ru.akvine.wild.bot.services.AdvertStatisticService;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertBudgetInfoResponse;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertChangeCpmRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.ChangeStocksRequest;
import ru.akvine.wild.bot.services.outbox.BotMessageOutbox;
import ru.akvine.wild.bot.services.property.PropertyService;

@DisplayName("Плановые джобы: проверка кампаний, подписки, очистка")
class ScheduledJobsTest {
    private static final String TOKEN = "wb-token";

    private AdvertRepository advertRepository;
    private BotMessageOutbox outbox;
    private WildberriesIntegrationService wb;
    private CountersStorage counters;
    private AdvertStatisticService statisticService;
    private PropertyService properties;
    private TransactionTemplate transactionTemplate;
    private CheckRunningAdvertsJob checkJob;

    private Map<String, Object> propertyValues;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        advertRepository = mock(AdvertRepository.class);
        outbox = mock(BotMessageOutbox.class);
        wb = mock(WildberriesIntegrationService.class);
        counters = mock(CountersStorage.class);
        statisticService = mock(AdvertStatisticService.class);
        properties = mock(PropertyService.class);
        transactionTemplate = mock(TransactionTemplate.class);
        doAnswer(invocation -> {
                    ((Consumer<Object>) invocation.getArgument(0)).accept(null);
                    return null;
                })
                .when(transactionTemplate)
                .executeWithoutResult(any());

        propertyValues = Map.of(
                "max.start.sum.difference", 300,
                "advert.max.cpm", 500,
                "check.advert.iterations.before.increase", 2,
                "advert.cpm.increase.value", 10,
                "wildberries.warehouse.id", 77,
                "check.advert.cron.milliseconds", 60_000L);
        when(properties.getAs(anyString(), any(Class.class)))
                .thenAnswer(invocation -> propertyValues.get(invocation.getArgument(0)));

        checkJob = new CheckRunningAdvertsJob(
                advertRepository,
                outbox,
                wb,
                counters,
                statisticService,
                properties,
                transactionTemplate,
                "job",
                "system",
                "system");
    }

    private AdvertEntity runningAdvert(int cpm, int startBudget) {
        ClientEntity client =
                new ClientEntity().setChatId("100").setBotType(BotType.TELEGRAM).setToken(TOKEN);
        CardEntity card =
                new CardEntity().setBarcode("barcode").setCategoryId(9).setOwnerClient(client);
        card.setUuid("card-uuid");
        AdvertEntity advert = new AdvertEntity()
                .setExternalId(555)
                .setStatus(AdvertStatus.RUNNING)
                .setCpm(cpm)
                .setStartBudgetSum(startBudget)
                .setStartCheckDateTime(LocalDateTime.of(2024, 5, 1, 10, 0))
                .setCard(card);
        when(advertRepository.findByStatuses(List.of(AdvertStatus.RUNNING))).thenReturn(List.of(advert));
        return advert;
    }

    private void budgetIs(int total) {
        when(wb.getAdvertBudgetInfo(555, TOKEN)).thenReturn(new AdvertBudgetInfoResponse().setTotal(total));
    }

    @Test
    @DisplayName(
            "Бюджет израсходован: статистика сохраняется, остатки обнуляются, кампания встаёт на паузу, клиент уведомлён")
    void exhaustedBudgetFinishesTest() {
        AdvertEntity advert = runningAdvert(100, 1000);
        budgetIs(0);

        checkJob.checkRunningAdverts();

        verify(wb, never()).pauseAdvert(anyInt(), anyString());
        verify(statisticService).getAndSave(eq(advert), any());
        ArgumentCaptor<ChangeStocksRequest> stocks = ArgumentCaptor.forClass(ChangeStocksRequest.class);
        verify(wb).changeStocks(stocks.capture(), eq(77), eq(TOKEN));
        assertThat(stocks.getValue().getStocks().get(0).getAmount()).isZero();
        assertThat(stocks.getValue().getStocks().get(0).getSku()).isEqualTo("barcode");
        assertThat(advert.getStatus()).isEqualTo(AdvertStatus.PAUSE);
        assertThat(advert.getNextCheckDateTime()).isNull();
        verify(advertRepository).save(advert);
        verify(outbox)
                .enqueue(
                        eq("100"),
                        eq(BotType.TELEGRAM),
                        org.mockito.ArgumentMatchers.contains("555"),
                        eq("advert-finished:555:2024-05-01T10:00"));
        verify(counters).delete(555);
    }

    @Test
    @DisplayName("Потрачено не меньше порога: кампания ставится на паузу в WB перед завершением теста")
    void spentLimitPausesAdvertInWildberries() {
        AdvertEntity advert = runningAdvert(100, 1000);
        budgetIs(600);

        checkJob.checkRunningAdverts();

        verify(wb).pauseAdvert(555, TOKEN);
        assertThat(advert.getStatus()).isEqualTo(AdvertStatus.PAUSE);
        verify(counters).delete(555);
    }

    @Test
    @DisplayName("Пришло время повышать ставку: ставка в WB и в БД увеличивается, счётчик итераций растёт")
    void cpmIsIncreasedWhenCounterAllows() {
        AdvertEntity advert = runningAdvert(100, 1000);
        budgetIs(900);
        when(counters.check(555, 2)).thenReturn(true);

        checkJob.checkRunningAdverts();

        ArgumentCaptor<AdvertChangeCpmRequest> request = ArgumentCaptor.forClass(AdvertChangeCpmRequest.class);
        verify(wb).changeAdvertCpm(request.capture(), eq(TOKEN));
        assertThat(request.getValue().getCpm()).isEqualTo(110);
        assertThat(request.getValue().getAdvertId()).isEqualTo(555);
        assertThat(request.getValue().getParam()).isEqualTo(9);
        assertThat(advert.getCpm()).isEqualTo(110);
        assertThat(advert.getCheckBudgetSum()).isEqualTo(900);
        assertThat(advert.getNextCheckDateTime()).isAfter(LocalDateTime.now().minusMinutes(1));
        verify(counters).increase(555);
        verify(advertRepository).save(advert);
    }

    @Test
    @DisplayName("Ещё не время повышать ставку или ставка уже максимальная: ставка не меняется")
    void cpmIsNotChanged() {
        AdvertEntity young = runningAdvert(100, 1000);
        budgetIs(900);
        when(counters.check(555, 2)).thenReturn(false);

        checkJob.checkRunningAdverts();

        verify(wb, never()).changeAdvertCpm(any(), anyString());
        assertThat(young.getCpm()).isEqualTo(100);
        verify(counters).increase(555);

        org.mockito.Mockito.clearInvocations(wb, counters);
        AdvertEntity maxed = runningAdvert(500, 1000);
        budgetIs(900);

        checkJob.checkRunningAdverts();

        verify(wb, never()).changeAdvertCpm(any(), anyString());
        verify(counters, never()).increase(anyInt());
        assertThat(maxed.getNextCheckDateTime()).isNotNull();
    }

    @Test
    @DisplayName("Нет работающих кампаний: ничего не вызывается")
    void noRunningAdverts() {
        when(advertRepository.findByStatuses(List.of(AdvertStatus.RUNNING))).thenReturn(List.of());

        checkJob.checkRunningAdverts();

        verify(wb, never()).getAdvertBudgetInfo(anyInt(), anyString());
    }

    @Test
    @DisplayName("Подписки: просроченные удаляются, непросроченные остаются")
    void expiredSubscriptionsAreDeleted() {
        SubscriptionRepository repository = mock(SubscriptionRepository.class);
        SubscriptionJob job = new SubscriptionJob(outbox, repository, "job", "system");
        SubscriptionEntity expired = subscription(LocalDateTime.now().minusDays(1), false);
        SubscriptionEntity active = subscription(LocalDateTime.now().plusDays(30), false);
        when(repository.findAll()).thenReturn(List.of(expired, active));

        job.deleteExpiredSubscriptions();

        verify(repository).delete(expired);
        verify(repository, never()).delete(active);
    }

    @Test
    @DisplayName("Подписки: клиент, у которого подписка скоро кончится, уведомляется один раз")
    void expiringSubscriptionsAreNotifiedOnce() {
        SubscriptionRepository repository = mock(SubscriptionRepository.class);
        SubscriptionJob job = new SubscriptionJob(outbox, repository, "job", "system");
        ReflectionTestUtils.setField(job, "notifyDaysBefore", 3);
        SubscriptionEntity soon = subscription(LocalDateTime.now().plusDays(2).plusHours(1), false);
        SubscriptionEntity alreadyNotified = subscription(LocalDateTime.now().plusDays(2), true);
        SubscriptionEntity far = subscription(LocalDateTime.now().plusDays(30), false);
        SubscriptionEntity expired = subscription(LocalDateTime.now().minusDays(1), false);
        when(repository.findAll()).thenReturn(List.of(soon, alreadyNotified, far, expired));

        job.notifyClients();

        assertThat(soon.isNotifiedThatExpires()).isTrue();
        assertThat(far.isNotifiedThatExpires()).isFalse();
        assertThat(expired.isNotifiedThatExpires()).isFalse();
        verify(repository).save(soon);
        verify(repository, never()).save(far);
        verify(outbox)
                .enqueue(
                        eq("100"),
                        eq(BotType.TELEGRAM),
                        org.mockito.ArgumentMatchers.contains("2 дня"),
                        eq("subscription-expiring:7"));
    }

    @Test
    @DisplayName("Очистка: удалённые кампании со статистикой стираются навсегда; пустой список ничего не трогает")
    void deleteAdvertsAndStatistics() {
        AdvertRepository adverts = mock(AdvertRepository.class);
        AdvertStatisticRepository statistics = mock(AdvertStatisticRepository.class);
        DeleteAdvertsAndStatisticsJob job =
                new DeleteAdvertsAndStatisticsJob(statistics, adverts, "job", "system", "system");
        ReflectionTestUtils.setField(job, "afterDaysExpiredCount", 30);
        AdvertEntity first = new AdvertEntity();
        first.setId(1L);
        AdvertEntity second = new AdvertEntity();
        second.setId(2L);
        when(adverts.findDeletedAfterExpiringDateCome(any()))
                .thenReturn(List.of(first, second))
                .thenReturn(List.of());

        job.delete();
        job.delete();

        ArgumentCaptor<Collection<Long>> ids = ArgumentCaptor.forClass(Collection.class);
        verify(statistics).deleteByAdvertIds(ids.capture());
        assertThat(ids.getValue()).containsExactly(1L, 2L);
        verify(adverts).deleteAll(ids.getValue());
    }

    private static SubscriptionEntity subscription(LocalDateTime expiresAt, boolean notified) {
        ClientEntity client = new ClientEntity().setChatId("100").setBotType(BotType.TELEGRAM);
        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setId(7L);
        subscription.setClient(client);
        subscription.setExpiresAt(expiresAt);
        subscription.setNotifiedThatExpires(notified);
        return subscription;
    }
}
