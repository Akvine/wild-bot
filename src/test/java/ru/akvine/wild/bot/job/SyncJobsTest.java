package ru.akvine.wild.bot.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import ru.akvine.wild.bot.entities.AdvertEntity;
import ru.akvine.wild.bot.entities.CardEntity;
import ru.akvine.wild.bot.entities.CardTypeEntity;
import ru.akvine.wild.bot.enums.AdvertStatus;
import ru.akvine.wild.bot.exceptions.PropertySyncException;
import ru.akvine.wild.bot.infrastructure.lock.DistributedLockProvider;
import ru.akvine.wild.bot.infrastructure.property.printers.PropertiesPrinter;
import ru.akvine.wild.bot.job.sync.GlobalSyncJob;
import ru.akvine.wild.bot.job.sync.SyncAdvertJob;
import ru.akvine.wild.bot.job.sync.SyncCardJob;
import ru.akvine.wild.bot.job.sync.SyncCardTypeJob;
import ru.akvine.wild.bot.job.sync.SyncPropertiesJob;
import ru.akvine.wild.bot.repositories.AdvertRepository;
import ru.akvine.wild.bot.repositories.CardRepository;
import ru.akvine.wild.bot.repositories.CardTypeRepository;
import ru.akvine.wild.bot.services.AdvertService;
import ru.akvine.wild.bot.services.CardService;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.domain.ClientModel;
import ru.akvine.wild.bot.services.integration.custodian.CustodianIntegrationService;
import ru.akvine.wild.bot.services.integration.custodian.dto.GetPropertiesRequest;
import ru.akvine.wild.bot.services.integration.custodian.dto.PropertyDto;
import ru.akvine.wild.bot.services.integration.custodian.dto.PropertyResponse;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertListResponse;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertParams;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertStatisticDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertSubject;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertsInfoResponse;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.CardDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.type.CardTypeResponse;
import ru.akvine.wild.bot.services.property.PropertyService;

@DisplayName("Джобы синхронизации с Wildberries и Custodian")
class SyncJobsTest {
    private ExecutorService executor;
    private WildberriesIntegrationService wb;
    private ClientService clientService;

    @BeforeEach
    void setUp() {
        executor = Executors.newFixedThreadPool(2);
        wb = mock(WildberriesIntegrationService.class);
        clientService = mock(ClientService.class);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    private static ClientModel client(long id, String uuid) {
        return new ClientModel().setId(id).setUuid(uuid).setToken("token-" + uuid);
    }

    private static AdvertStatisticDto group(int status, Integer... advertIds) {
        return new AdvertStatisticDto()
                .setStatus(status)
                .setAdvertList(java.util.Arrays.stream(advertIds)
                        .map(id -> new AdvertDto().setAdvertId(id))
                        .toList());
    }

    private static AdvertDto advertInfo(int id, int status, boolean withSubject) {
        return new AdvertDto()
                .setAdvertId(id)
                .setStatus(status)
                .setAdvertParams(withSubject ? new AdvertParams().setSubject(new AdvertSubject().setId(1)) : null);
    }

    private SyncAdvertJob advertJob(
            AdvertRepository repository, AdvertService advertService, DistributedLockProvider lockProvider) {
        return new SyncAdvertJob(repository, advertService, clientService, wb, lockProvider, executor);
    }

    private static DistributedLockProvider runningLock() {
        DistributedLockProvider lock = mock(DistributedLockProvider.class);
        doAnswer(invocation -> {
                    ((Runnable) invocation.getArgument(1)).run();
                    return null;
                })
                .when(lock)
                .lock(anyString(), any(Runnable.class));
        return lock;
    }

    @Test
    @DisplayName("Кампании: лишние в БД помечаются удалёнными, новые из WB загружаются пачками и сохраняются")
    void advertSyncDeletesStaleAndSavesNewOnes() {
        AdvertRepository repository = mock(AdvertRepository.class);
        AdvertService advertService = mock(AdvertService.class);
        when(clientService.getAllActive()).thenReturn(List.of(client(1, "c1")));
        int pause = AdvertStatus.PAUSE.getCode();
        when(wb.getAdverts("token-c1"))
                .thenReturn(new AdvertListResponse()
                        .setAll(3)
                        .setAdverts(List.of(group(pause, 10, 11), group(AdvertStatus.RUNNING.getCode(), 99))));
        AdvertEntity stale = new AdvertEntity().setExternalId(20);
        AdvertEntity common = new AdvertEntity().setExternalId(10);
        when(repository.findByClientIdAndStatuses(eq(1L), anyList())).thenReturn(List.of(stale, common));
        when(wb.getAdvertsInfo(List.of(11), "token-c1"))
                .thenReturn(new AdvertsInfoResponse()
                        .setAdverts(List.of(
                                advertInfo(11, pause, true),
                                advertInfo(12, pause, false),
                                advertInfo(13, AdvertStatus.RUNNING.getCode(), true))));

        advertJob(repository, advertService, runningLock()).sync();

        assertThat(stale.isDeleted()).isTrue();
        assertThat(common.isDeleted()).isFalse();
        verify(repository).save(stale);
        verify(repository, never()).save(common);
        ArgumentCaptor<List<AdvertDto>> saved = ArgumentCaptor.forClass(List.class);
        verify(advertService).saveAll(saved.capture());
        assertThat(saved.getValue()).extracting(AdvertDto::getAdvertId).containsExactly(11);
    }

    @Test
    @DisplayName("Кампании: большой список загружается пачками по 50")
    void advertSyncBatchesBy50() {
        AdvertRepository repository = mock(AdvertRepository.class);
        AdvertService advertService = mock(AdvertService.class);
        when(clientService.getAllActive()).thenReturn(List.of(client(1, "c1")));
        Integer[] ids = IntStream.rangeClosed(1, 120).boxed().toArray(Integer[]::new);
        when(wb.getAdverts("token-c1"))
                .thenReturn(new AdvertListResponse().setAll(120).setAdverts(List.of(group(11, ids))));
        when(repository.findByClientIdAndStatuses(eq(1L), anyList())).thenReturn(List.of());
        when(wb.getAdvertsInfo(anyList(), eq("token-c1"))).thenReturn(new AdvertsInfoResponse().setAdverts(List.of()));

        advertJob(repository, advertService, runningLock()).sync();

        verify(wb, times(3)).getAdvertsInfo(anyList(), eq("token-c1"));
        verify(advertService, times(3)).saveAll(anyList());
    }

    @Test
    @DisplayName("Кампании: пустой список в WB ничего не меняет, сбой одного клиента не мешает остальным")
    void advertSyncSkipsEmptyAndIsolatesFailures() {
        AdvertRepository repository = mock(AdvertRepository.class);
        AdvertService advertService = mock(AdvertService.class);
        when(clientService.getAllActive()).thenReturn(List.of(client(1, "ok"), client(2, "bad")));
        when(wb.getAdverts("token-ok")).thenReturn(new AdvertListResponse().setAll(0));
        when(wb.getAdverts("token-bad")).thenThrow(new IllegalStateException("wb is down"));

        advertJob(repository, advertService, runningLock()).sync();

        verify(wb).getAdverts("token-ok");
        verify(repository, never()).findByClientIdAndStatuses(any(), anyList());
        verify(advertService, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Карточки: удалённые в WB помечаются удалёнными, новые создаются, пустой ответ пропускается")
    void cardSync() {
        CardRepository cardRepository = mock(CardRepository.class);
        CardService cardService = mock(CardService.class);
        when(clientService.getAllActive()).thenReturn(List.of(client(1, "c1"), client(2, "empty"), client(3, "bad")));
        CardDto keep = new CardDto().setNmID(1);
        CardDto fresh = new CardDto().setNmID(2);
        when(wb.getCards("token-c1")).thenReturn(List.of(keep, fresh));
        when(wb.getCards("token-empty")).thenReturn(List.of());
        when(wb.getCards("token-bad")).thenThrow(new IllegalStateException("boom"));
        CardEntity existing = new CardEntity().setExternalId(1);
        CardEntity stale = new CardEntity().setExternalId(5);
        when(cardRepository.findAll("c1")).thenReturn(List.of(existing, stale));

        new SyncCardJob(wb, cardRepository, cardService, clientService, executor).sync();

        assertThat(stale.isDeleted()).isTrue();
        assertThat(existing.isDeleted()).isFalse();
        verify(cardRepository).save(stale);
        ArgumentCaptor<List<CardDto>> created = ArgumentCaptor.forClass(List.class);
        verify(cardService).create(created.capture(), eq("c1"));
        assertThat(created.getValue()).containsExactly(fresh);
        verify(cardRepository, never()).findAll("empty");
    }

    @Test
    @DisplayName("Типы карточек: новые типы сохраняются один раз на объединённом списке")
    void cardTypeSync() {
        CardTypeRepository repository = mock(CardTypeRepository.class);
        when(clientService.getAllActive()).thenReturn(List.of(client(1, "a"), client(2, "b")));
        when(wb.getTypes("token-a")).thenReturn(new CardTypeResponse().setData(List.of("Женский", "Мужской")));
        when(wb.getTypes("token-b")).thenReturn(new CardTypeResponse().setData(List.of("Мужской", "Детский")));
        when(repository.findAll())
                .thenReturn(List.of(new CardTypeEntity().setType("Женский"), new CardTypeEntity().setType("Старый")));

        new SyncCardTypeJob(wb, repository, clientService, executor).sync();

        ArgumentCaptor<CardTypeEntity> saved = ArgumentCaptor.forClass(CardTypeEntity.class);
        verify(repository, times(2)).save(saved.capture());
        assertThat(saved.getAllValues())
                .extracting(CardTypeEntity::getType)
                .containsExactlyInAnyOrder("Мужской", "Детский");
    }

    @Test
    @DisplayName("Типы карточек: если WB не ответил ни одному клиенту, справочник не трогается")
    void cardTypeSyncKeepsDatabaseWhenAllRequestsFail() {
        CardTypeRepository repository = mock(CardTypeRepository.class);
        when(clientService.getAllActive()).thenReturn(List.of(client(1, "a")));
        when(wb.getTypes(anyString())).thenThrow(new IllegalStateException("down"));

        new SyncCardTypeJob(wb, repository, clientService, executor).sync();

        verify(repository, never()).findAll();
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Настройки из Custodian: известные ключи применяются, неизменяемые пропускаются")
    void propertiesSyncAppliesKnownAndSkipsImmutable() {
        PropertyService propertyService = mock(PropertyService.class);
        CustodianIntegrationService custodian = mock(CustodianIntegrationService.class);
        SyncPropertiesJob job = new SyncPropertiesJob(propertyService, custodian);
        ReflectionTestUtils.setField(job, "activeProfile", "prod");
        ReflectionTestUtils.setField(job, "validationEnabled", true);
        when(propertyService.contains(anyString())).thenReturn(true);
        when(custodian.getProperties(any(GetPropertiesRequest.class)))
                .thenReturn(new PropertyResponse()
                        .setCount(2)
                        .setProperties(List.of(
                                new PropertyDto().setKey("some.key").setValue("1"),
                                new PropertyDto()
                                        .setKey("check.advert.cron.milliseconds")
                                        .setValue("5"))));

        job.sync();

        verify(propertyService).put("some.key", "1");
        verify(propertyService, never()).put("check.advert.cron.milliseconds", "5");
        ArgumentCaptor<GetPropertiesRequest> request = ArgumentCaptor.forClass(GetPropertiesRequest.class);
        verify(custodian).getProperties(request.capture());
        assertThat(request.getValue().getProfile()).isEqualTo("prod");
    }

    @Test
    @DisplayName("Настройки из Custodian: расхождение с локальным хранилищем при включённой проверке - ошибка")
    void propertiesSyncFailsOnUnknownKey() {
        PropertyService propertyService = mock(PropertyService.class);
        CustodianIntegrationService custodian = mock(CustodianIntegrationService.class);
        SyncPropertiesJob job = new SyncPropertiesJob(propertyService, custodian);
        ReflectionTestUtils.setField(job, "activeProfile", "prod");
        ReflectionTestUtils.setField(job, "validationEnabled", true);
        when(propertyService.contains("unknown")).thenReturn(false);
        when(custodian.getProperties(any()))
                .thenReturn(new PropertyResponse()
                        .setProperties(
                                List.of(new PropertyDto().setKey("unknown").setValue("v"))));

        assertThatThrownBy(job::sync).isInstanceOf(PropertySyncException.class).hasMessageContaining("unknown");

        ReflectionTestUtils.setField(job, "validationEnabled", false);
        job.sync();
        verify(propertyService).put("unknown", "v");
    }

    @Test
    @DisplayName("Печать настроек: при старте или по расписанию в зависимости от флага")
    void printPropertiesJob() {
        PropertyService propertyService = mock(PropertyService.class);
        PropertiesPrinter printer = mock(PropertiesPrinter.class);
        PrintPropertiesJob atStart = new PrintPropertiesJob(propertyService, printer);
        ReflectionTestUtils.setField(atStart, "printPropertiesOnlyAtStart", true);

        ReflectionTestUtils.invokeMethod(atStart, "init");
        atStart.printProperties();
        verify(printer, times(1)).print(any());

        PrintPropertiesJob scheduled = new PrintPropertiesJob(propertyService, printer);
        ReflectionTestUtils.setField(scheduled, "printPropertiesOnlyAtStart", false);
        ReflectionTestUtils.invokeMethod(scheduled, "init");
        scheduled.printProperties();
        verify(printer, times(2)).print(any());
    }

    @Test
    @DisplayName("Глобальная синхронизация запускает только включённые шаги")
    void globalSyncRunsEnabledSteps() {
        SyncCardTypeJob types = mock(SyncCardTypeJob.class);
        SyncCardJob cards = mock(SyncCardJob.class);
        SyncAdvertJob adverts = mock(SyncAdvertJob.class);
        GlobalSyncJob job = new GlobalSyncJob(types, cards, adverts, "GlobalSyncJob", "system");

        ReflectionTestUtils.setField(job, "syncCardTypesEnabled", true);
        ReflectionTestUtils.setField(job, "syncCardsEnabled", false);
        ReflectionTestUtils.setField(job, "syncAdvertsEnabled", true);
        job.globalSync();
        verify(types).sync();
        verify(cards, never()).sync();
        verify(adverts).sync();

        ReflectionTestUtils.setField(job, "syncCardTypesEnabled", false);
        ReflectionTestUtils.setField(job, "syncCardsEnabled", true);
        ReflectionTestUtils.setField(job, "syncAdvertsEnabled", false);
        job.globalSync();
        verify(cards).sync();
        verify(types, times(1)).sync();
    }
}
