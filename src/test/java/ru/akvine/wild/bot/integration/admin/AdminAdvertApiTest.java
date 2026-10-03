package ru.akvine.wild.bot.integration.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.akvine.wild.bot.entities.AdvertEntity;
import ru.akvine.wild.bot.entities.CardEntity;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.enums.AdvertStatus;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertFullStatisticResponse;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertsInfoResponse;

@DisplayName("Админский REST: кампании")
class AdminAdvertApiTest extends AdminApiBaseTest {

    private record Setup(ClientEntity client, CardEntity card, AdvertEntity advert) {}

    private Setup runningAdvert() {
        ClientEntity client = newClientWithToken();
        CardEntity card = newCard(client);
        AdvertEntity advert = newAdvert(card, AdvertStatus.RUNNING);
        newStatistic(advert, client);
        return new Setup(client, card, advert);
    }

    private void wbReports(AdvertEntity advert, int wbStatus) {
        when(wildberriesIntegrationService.getAdvertsInfo(eq(List.of(advert.getExternalId())), anyString()))
                .thenReturn(new AdvertsInfoResponse()
                        .setAdverts(List.of(new AdvertDto()
                                .setAdvertId(advert.getExternalId())
                                .setStatus(wbStatus))));
        AdvertFullStatisticResponse statistic = new AdvertFullStatisticResponse()
                .setViews("100")
                .setClicks("10")
                .setCtr("10.0")
                .setCpc("1.5")
                .setSum("15")
                .setAtbs("1")
                .setOrders("2")
                .setCr("20")
                .setShks("2")
                .setSumPrice("300");
        when(wildberriesIntegrationService.getAdvertsFullStatisticByDates(anyList(), anyString()))
                .thenReturn(new AdvertFullStatisticResponse[] {statistic});
        when(wildberriesIntegrationService.getAdvertsFullStatisticByInterval(anyList(), anyString()))
                .thenReturn(new AdvertFullStatisticResponse[] {statistic});
    }

    @Test
    @DisplayName("Список кампаний по статусам: фикстуры в статусе PAUSE попадают в ответ")
    void listByStatuses() throws Exception {
        postJson("/admin/adverts/list", "{\"statuses\":[\"pause\"]}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.count").isNumber())
                .andExpect(jsonPath("$.adverts").isArray());
    }

    @Test
    @DisplayName("Список кампаний: пустой, неизвестный и отсутствующий статус - ошибки")
    void listValidation() throws Exception {
        postJson("/admin/adverts/list", "{\"statuses\":[\"\"]}").andExpect(status().isBadRequest());
        postJson("/admin/adverts/list", "{\"statuses\":[\"flying\"]}").andExpect(status().isBadRequest());
        postJson("/admin/adverts/list", "{}").andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName(
            "Пауза кампании: WB ставится на паузу, статистика сохраняется, статус и уведомление клиенту обновляются")
    void pauseRunningAdvert() throws Exception {
        Setup setup = runningAdvert();
        wbReports(setup.advert(), AdvertStatus.RUNNING.getCode());

        postJson(
                        "/admin/adverts/pause",
                        "{\"clientUuid\":\"" + setup.client().getUuid() + "\",\"advertUuid\":\""
                                + setup.advert().getUuid() + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.advertStatistic.views").value("100"));

        verify(wildberriesIntegrationService).pauseAdvert(setup.advert().getExternalId(), "encrypted-token");
        AdvertEntity paused = advertRepository.findById(setup.advert().getId()).orElseThrow();
        assertThat(paused.getStatus()).isEqualTo(AdvertStatus.PAUSE);
        assertThat(paused.getNextCheckDateTime()).isNull();
        verify(botMessageOutbox)
                .enqueue(
                        eq(setup.client().getChatId()),
                        any(),
                        org.mockito.ArgumentMatchers.contains("успешно завершился"),
                        any());
    }

    @Test
    @DisplayName("Пауза по внешнему id кампании; если WB уже на паузе, повторная пауза в WB не вызывается")
    void pauseByExternalIdWhenWbAlreadyPaused() throws Exception {
        Setup setup = runningAdvert();
        wbReports(setup.advert(), AdvertStatus.PAUSE.getCode());

        postJson(
                        "/admin/adverts/pause",
                        "{\"clientUuid\":\"" + setup.client().getUuid() + "\",\"advertId\":"
                                + setup.advert().getExternalId() + "}")
                .andExpect(status().isOk());

        verify(wildberriesIntegrationService, never())
                .pauseAdvert(eq(setup.advert().getExternalId()), anyString());
    }

    @Test
    @DisplayName("Пауза: кампания уже на паузе - ошибка, пустой ответ WB - ошибка, неверные идентификаторы - ошибка")
    void pauseFailures() throws Exception {
        ClientEntity client = newClientWithToken();
        AdvertEntity paused = newAdvert(newCard(client), AdvertStatus.PAUSE);
        postJson(
                        "/admin/adverts/pause",
                        "{\"clientUuid\":\"" + client.getUuid() + "\",\"advertUuid\":\"" + paused.getUuid() + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"));

        Setup setup = runningAdvert();
        when(wildberriesIntegrationService.getAdvertsInfo(anyList(), anyString()))
                .thenReturn(new AdvertsInfoResponse().setAdverts(List.of()));
        postJson(
                        "/admin/adverts/pause",
                        "{\"clientUuid\":\"" + setup.client().getUuid() + "\",\"advertUuid\":\""
                                + setup.advert().getUuid() + "\"}")
                .andExpect(status().isBadRequest());

        postJson("/admin/adverts/pause", "{\"clientUuid\":\"x\"}").andExpect(status().isBadRequest());
        postJson("/admin/adverts/pause", "{\"clientUuid\":\"x\",\"advertUuid\":\"y\",\"advertId\":1}")
                .andExpect(status().isBadRequest());
        postJson("/admin/adverts/pause", "{\"clientUuid\":\"missing\",\"advertId\":1}")
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Принудительная пауза: статистика удаляется, кампания недоступна до начала следующих суток")
    void pauseForce() throws Exception {
        Setup setup = runningAdvert();
        wbReports(setup.advert(), AdvertStatus.RUNNING.getCode());

        postJson(
                        "/admin/adverts/pause/force",
                        "{\"advertUuid\":\"" + setup.advert().getUuid() + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        AdvertEntity paused = advertRepository.findById(setup.advert().getId()).orElseThrow();
        assertThat(paused.getStatus()).isEqualTo(AdvertStatus.PAUSE);
        assertThat(paused.getAvailableForStart()).isAfter(LocalDateTime.now());
        assertThat(advertStatisticRepository.findByClientIdAndAdvertId(
                        setup.client().getId(), setup.advert().getId()))
                .isEmpty();
        verify(wildberriesIntegrationService).pauseAdvert(setup.advert().getExternalId(), "encrypted-token");
    }

    @Test
    @DisplayName("Принудительная пауза по внешнему id; уже поставленная на паузу и пустой ответ WB - ошибки")
    void pauseForceFailures() throws Exception {
        Setup setup = runningAdvert();
        when(wildberriesIntegrationService.getAdvertsInfo(anyList(), anyString()))
                .thenReturn(new AdvertsInfoResponse().setAdverts(List.of()));
        postJson("/admin/adverts/pause/force", "{\"advertId\":" + setup.advert().getExternalId() + "}")
                .andExpect(status().isBadRequest());

        AdvertEntity paused = newAdvert(setup.card(), AdvertStatus.PAUSE);
        postJson("/admin/adverts/pause/force", "{\"advertId\":" + paused.getExternalId() + "}")
                .andExpect(status().isBadRequest());
        postJson("/admin/adverts/pause/force", "{}").andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Переименование кампании: имя уходит в WB и сохраняется в БД")
    void rename() throws Exception {
        Setup setup = runningAdvert();

        postJson(
                        "/admin/adverts/rename",
                        "{\"clientUuid\":\"" + setup.client().getUuid() + "\",\"advertId\":"
                                + setup.advert().getExternalId() + ",\"name\":\"Новое имя\"}")
                .andExpect(status().isOk());

        verify(wildberriesIntegrationService)
                .renameAdvert(setup.advert().getExternalId(), "Новое имя", "encrypted-token");
        assertThat(advertRepository
                        .findById(setup.advert().getId())
                        .orElseThrow()
                        .getExternalTitle())
                .isEqualTo("Новое имя");

        postJson(
                        "/admin/adverts/rename",
                        "{\"clientUuid\":\"" + setup.client().getUuid() + "\",\"advertUuid\":\""
                                + setup.advert().getUuid() + "\",\"name\":\"Ещё\"}")
                .andExpect(status().isOk());
        postJson("/admin/adverts/rename", "{\"clientUuid\":\"x\",\"advertId\":1}")
                .andExpect(status().isBadRequest());
        postJson("/admin/adverts/rename", "{\"clientUuid\":\"x\",\"name\":\"n\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Обновление времени доступности кампании")
    void updateAvailableForStart() throws Exception {
        Setup setup = runningAdvert();

        putJson(
                        "/admin/adverts",
                        "{\"advertId\":" + setup.advert().getExternalId()
                                + ",\"availableForStart\":\"2035-01-02T03:04:05\"}")
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.adverts[0].advertId").value(setup.advert().getExternalId()));

        assertThat(advertRepository
                        .findById(setup.advert().getId())
                        .orElseThrow()
                        .getAvailableForStart())
                .isEqualTo(LocalDateTime.of(2035, 1, 2, 3, 4, 5));

        putJson("/admin/adverts", "{\"advertId\":" + setup.advert().getExternalId() + "}")
                .andExpect(status().isOk());
        putJson("/admin/adverts", "{\"advertId\":-12345}").andExpect(status().isBadRequest());
        verify(wildberriesIntegrationService, never()).startAdvert(any(Integer.class), anyString());
    }
}
