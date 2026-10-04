package ru.akvine.wild.bot.integration.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.akvine.wild.bot.entities.AdvertEntity;
import ru.akvine.wild.bot.entities.CardEntity;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.enums.AdvertStatus;
import ru.akvine.wild.bot.enums.AdvertType;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.exceptions.AdvertNotFoundException;
import ru.akvine.wild.bot.exceptions.CardNotFoundException;
import ru.akvine.wild.bot.integration.base.BaseTest;
import ru.akvine.wild.bot.repositories.AdvertRepository;
import ru.akvine.wild.bot.repositories.CardRepository;
import ru.akvine.wild.bot.repositories.CardTypeRepository;
import ru.akvine.wild.bot.repositories.ClientRepository;
import ru.akvine.wild.bot.services.AdvertService;
import ru.akvine.wild.bot.services.domain.AdvertModel;
import ru.akvine.wild.bot.services.domain.ClientModel;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertParams;
import ru.akvine.wild.bot.services.property.PropertyService;
import ru.akvine.wild.bot.utils.UUIDGenerator;

@DisplayName("AdvertService: сохранение кампаний из Wildberries, обновление, поиск и выбор кампании для запуска")
class AdvertServiceTest extends BaseTest {
    private static final AtomicInteger IDS = new AtomicInteger(11_000_000);
    private static final String CREATE_BY_API = "create.adverts.by.api.enabled";

    @Autowired
    private AdvertService advertService;

    @Autowired
    private AdvertRepository advertRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardTypeRepository cardTypeRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PropertyService propertyService;

    @AfterEach
    void restoreProperty() {
        propertyService.put(CREATE_BY_API, "true");
    }

    private ClientEntity client() {
        String chatId = String.valueOf(ThreadLocalRandom.current().nextLong(1_000_000_000L, Long.MAX_VALUE));
        return clientRepository.save(new ClientEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setChatId(chatId)
                .setFirstName("First")
                .setBotType(BotType.TELEGRAM)
                .setToken("token")
                .setInWhitelist(true));
    }

    private CardEntity card(ClientEntity owner, int categoryId, String type) {
        return cardRepository.save(new CardEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setExternalId(IDS.incrementAndGet())
                .setExternalTitle("Card")
                .setCategoryId(categoryId)
                .setCategoryTitle("Category")
                .setBarcode("barcode")
                .setOwnerClient(owner)
                .setCardType(cardTypeRepository.findByType(type).orElseThrow()));
    }

    private AdvertEntity advert(CardEntity card, AdvertStatus status, LocalDateTime availableForStart) {
        return advertRepository.save(new AdvertEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setExternalId(IDS.incrementAndGet())
                .setExternalTitle("Advert")
                .setChangeTime(new Date())
                .setStatus(status)
                .setOrdinalStatus(status.getCode())
                .setType(AdvertType.AUTO)
                .setOrdinalType(AdvertType.AUTO.getCode())
                .setCpm(100)
                .setAvailableForStart(availableForStart)
                .setCard(card));
    }

    private static int category() {
        return 20_000 + IDS.incrementAndGet() % 50_000;
    }

    @Test
    @DisplayName("Сохранение кампаний из Wildberries: привязка к карточке по первому nm, ставка из параметров")
    void saveAllCreatesAdverts() {
        ClientEntity owner = client();
        CardEntity card = card(owner, category(), "Мужской");
        int advertId = IDS.incrementAndGet();
        AdvertDto dto = new AdvertDto()
                .setAdvertId(advertId)
                .setName("From WB")
                .setChangeTime(new Date())
                .setType(AdvertType.AUTO.getCode())
                .setStatus(AdvertStatus.PAUSE.getCode())
                .setAdvertParams(new AdvertParams().setCpm(250).setNms(List.of(card.getExternalId())));

        advertService.saveAll(List.of(dto));

        AdvertEntity saved = advertRepository.findByExternalId(advertId).orElseThrow();
        assertThat(saved.getExternalTitle()).isEqualTo("From WB");
        assertThat(saved.getCpm()).isEqualTo(250);
        assertThat(saved.getStatus()).isEqualTo(AdvertStatus.PAUSE);
        assertThat(saved.getType()).isEqualTo(AdvertType.AUTO);
        assertThat(saved.getCard().getId()).isEqualTo(card.getId());
        assertThat(saved.getStartBudgetSum()).isNull();
    }

    @Test
    @DisplayName("Сохранение: если карточки с таким nm нет, кампания не сохраняется и бросается ошибка")
    void saveAllRejectsUnknownCard() {
        AdvertDto dto = new AdvertDto()
                .setAdvertId(IDS.incrementAndGet())
                .setType(AdvertType.AUTO.getCode())
                .setStatus(AdvertStatus.PAUSE.getCode())
                .setAdvertParams(new AdvertParams().setNms(List.of(-777_777)));

        assertThatThrownBy(() -> advertService.saveAll(List.of(dto))).isInstanceOf(CardNotFoundException.class);
        assertThatThrownBy(() -> advertService.saveAll(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Обновление: изменения применяются к существующей кампании; для неизвестной uuid - ошибка")
    void update() {
        ClientEntity owner = client();
        AdvertEntity advert = advert(card(owner, category(), "Мужской"), AdvertStatus.PAUSE, LocalDateTime.now());

        AdvertModel model = advertService.getByAdvertId(advert.getExternalId());
        model.setCpm(900).setStatus(AdvertStatus.RUNNING).setName("Renamed").setStartBudgetSum(1500);
        AdvertModel updated = advertService.update(model);

        assertThat(updated.getCpm()).isEqualTo(900);
        AdvertEntity reloaded = advertRepository.findById(advert.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(AdvertStatus.RUNNING);
        assertThat(reloaded.getOrdinalStatus()).isEqualTo(AdvertStatus.RUNNING.getCode());
        assertThat(reloaded.getExternalTitle()).isEqualTo("Renamed");
        assertThat(reloaded.getStartBudgetSum()).isEqualTo(1500);
        assertThat(reloaded.getUpdatedDate()).isNotNull();

        AdvertEntity transientAdvert = new AdvertEntity()
                .setUuid("no-such-uuid")
                .setStatus(AdvertStatus.PAUSE)
                .setType(AdvertType.AUTO)
                .setCard(advert.getCard());
        assertThatThrownBy(() -> advertService.update(new AdvertModel(transientAdvert)))
                .isInstanceOf(AdvertNotFoundException.class);
    }

    @Test
    @DisplayName("Поиск кампании: по внешнему id, uuid и паре кампания + клиент; отсутствующие - ошибки")
    void verifyExists() {
        ClientEntity owner = client();
        AdvertEntity advert = advert(card(owner, category(), "Мужской"), AdvertStatus.PAUSE, LocalDateTime.now());

        assertThat(advertService
                        .verifyExistsByExternalId(advert.getExternalId())
                        .getId())
                .isEqualTo(advert.getId());
        assertThat(advertService.verifyExistsByUuid(advert.getUuid()).getId()).isEqualTo(advert.getId());
        assertThat(advertService
                        .verifyExistsByAdvertIdAndClientId(advert.getExternalId(), owner.getId())
                        .getId())
                .isEqualTo(advert.getId());
        assertThat(advertService.getByAdvertId(advert.getExternalId()).getExternalId())
                .isEqualTo(advert.getExternalId());

        assertThatThrownBy(() -> advertService.verifyExistsByExternalId(-1_000_001))
                .isInstanceOf(AdvertNotFoundException.class);
        assertThatThrownBy(() -> advertService.verifyExistsByUuid("missing"))
                .isInstanceOf(AdvertNotFoundException.class);
        assertThatThrownBy(() -> advertService.verifyExistsByUuid(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> advertService.verifyExistsByAdvertIdAndClientId(advert.getExternalId(), -1L))
                .isInstanceOf(AdvertNotFoundException.class);
    }

    @Test
    @DisplayName("Списки: по статусам и по клиенту (chatId, тип бота, статусы)")
    void lists() {
        ClientEntity owner = client();
        CardEntity card = card(owner, category(), "Мужской");
        AdvertEntity paused = advert(card, AdvertStatus.PAUSE, LocalDateTime.now());
        AdvertEntity running = advert(card, AdvertStatus.RUNNING, LocalDateTime.now());

        assertThat(advertService.getAdvertsByStatuses(List.of(AdvertStatus.RUNNING)))
                .extracting(AdvertModel::getExternalId)
                .contains(running.getExternalId())
                .doesNotContain(paused.getExternalId());
        assertThat(advertService.getAdvertsByChatIdAndBotTypeAndStatuses(
                        owner.getChatId(), BotType.TELEGRAM, List.of(AdvertStatus.PAUSE)))
                .extracting(AdvertModel::getExternalId)
                .containsExactly(paused.getExternalId());
        assertThat(advertService.getByChatIdAndBotType(owner.getChatId(), BotType.TELEGRAM))
                .extracting(AdvertModel::getExternalId)
                .containsExactlyInAnyOrder(paused.getExternalId(), running.getExternalId());
        assertThatThrownBy(() -> advertService.getAdvertsByStatuses(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(
                        () -> advertService.getAdvertsByChatIdAndBotTypeAndStatuses(null, BotType.TELEGRAM, List.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName(
            "Выбор кампании для запуска: приоритет у PAUSE, затем READY_FOR_START; недоступные по времени пропускаются")
    void getFirstPrefersPauseThenReadyAndSkipsUnavailable() {
        ClientEntity owner = client();
        ClientModel clientModel = new ClientModel(owner);
        int category = category();
        CardEntity card = card(owner, category, "Мужской");
        advert(card, AdvertStatus.PAUSE, LocalDateTime.now().plusHours(1));
        AdvertEntity ready =
                advert(card, AdvertStatus.READY_FOR_START, LocalDateTime.now().minusMinutes(1));

        assertThat(advertService.getFirst("Мужской", category, clientModel).getExternalId())
                .isEqualTo(ready.getExternalId());

        AdvertEntity paused =
                advert(card, AdvertStatus.PAUSE, LocalDateTime.now().minusMinutes(1));
        assertThat(advertService.getFirst("Мужской", category, clientModel).getExternalId())
                .isEqualTo(paused.getExternalId());
        assertThatThrownBy(() -> advertService.getFirst(null, category, clientModel))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Выбор кампании: если подходящих нет и создание через API выключено - AdvertNotFoundException")
    void getFirstWithoutCandidatesAndApiDisabled() {
        propertyService.put(CREATE_BY_API, "false");
        ClientEntity owner = client();
        int category = category();
        card(owner, category, "Мужской");

        assertThatThrownBy(() -> advertService.getFirst("Мужской", category, new ClientModel(owner)))
                .isInstanceOf(AdvertNotFoundException.class)
                .hasMessageContaining(String.valueOf(category));
        verify(wildberriesIntegrationService, never()).createAdvert(any(), anyString());
    }

    /**
     * Характеризационный тест незавершённой ветки: при включённом создании через API кампания создаётся в Wildberries
     * (остатки и сама кампания), но потом сохраняется в БД без карточки (колонка CARD_ID обязательна), и вызов
     * заканчивается ошибкой, а созданная в WB кампания остаётся без записи у нас. Когда ветку доделают
     * (привязка карточки и возврат созданной кампании), тест нужно заменить проверкой нового поведения.
     */
    @Test
    @DisplayName("Создание кампании через API (ветка не доделана): вызовы в WB выполняются, сохранение в БД падает")
    void getFirstCreateByApiIsUnfinished() {
        propertyService.put(CREATE_BY_API, "true");
        ClientEntity owner = client();
        int category = category();
        card(owner, category, "Мужской");

        assertThatThrownBy(() -> advertService.getFirst("Мужской", category, new ClientModel(owner)))
                .isInstanceOf(RuntimeException.class);

        verify(wildberriesIntegrationService).changeStocks(any(), anyInt(), anyString());
        verify(wildberriesIntegrationService).createAdvert(any(), anyString());
    }
}
