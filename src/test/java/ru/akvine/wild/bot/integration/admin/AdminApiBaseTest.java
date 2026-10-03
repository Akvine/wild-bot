package ru.akvine.wild.bot.integration.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.akvine.wild.bot.entities.AdvertEntity;
import ru.akvine.wild.bot.entities.AdvertStatisticEntity;
import ru.akvine.wild.bot.entities.CardEntity;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.entities.SubscriptionEntity;
import ru.akvine.wild.bot.enums.AdvertStatus;
import ru.akvine.wild.bot.enums.AdvertType;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.integration.base.TelegramBaseTest;
import ru.akvine.wild.bot.repositories.AdvertRepository;
import ru.akvine.wild.bot.repositories.AdvertStatisticRepository;
import ru.akvine.wild.bot.repositories.CardRepository;
import ru.akvine.wild.bot.repositories.CardTypeRepository;
import ru.akvine.wild.bot.repositories.ClientRepository;
import ru.akvine.wild.bot.repositories.SubscriptionRepository;
import ru.akvine.wild.bot.utils.UUIDGenerator;

/**
 * Основа тестов админского REST: контроллеры, валидаторы, конвертеры, сервисы, репозитории и обработчик ошибок
 * работают по-настоящему на H2, а из внешних границ замокан только Telegram (см. {@link TelegramBaseTest}).
 * Фильтры безопасности отключены: аутентификация проверяется отдельно, здесь проверяется сама логика эндпоинтов.
 */
@AutoConfigureMockMvc(addFilters = false)
public abstract class AdminApiBaseTest extends TelegramBaseTest {
    @Autowired
    protected MockMvc mockMvc;

    /** insert ... on conflict из outbox - синтаксис PostgreSQL, H2 его не понимает; сам outbox проверяется отдельно */
    @MockBean
    protected ru.akvine.wild.bot.services.outbox.BotMessageOutbox botMessageOutbox;

    @Autowired
    protected ClientRepository clientRepository;

    @Autowired
    protected SubscriptionRepository subscriptionRepository;

    protected ResultActions postJson(String url, String json) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions getJson(String url, String json) throws Exception {
        return mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions putJson(String url, String json) throws Exception {
        return mockMvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    /** Уникальный числовой chatId: фикстуры занимают "1".."8", а тесты не должны мешать друг другу */
    protected static String uniqueChatId() {
        return String.valueOf(ThreadLocalRandom.current().nextLong(1_000_000_000L, Long.MAX_VALUE));
    }

    protected ClientEntity newClient(String chatId, boolean whitelisted) {
        return clientRepository.save(new ClientEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setChatId(chatId)
                .setUsername("user" + chatId)
                .setFirstName("First")
                .setLastName("Last")
                .setBotType(BotType.TELEGRAM)
                .setInWhitelist(whitelisted));
    }

    @Autowired
    protected CardRepository cardRepository;

    @Autowired
    protected ru.akvine.wild.bot.infrastructure.counter.CountersStorage countersStorage;

    @Autowired
    protected CardTypeRepository cardTypeRepository;

    @Autowired
    protected AdvertRepository advertRepository;

    @Autowired
    protected AdvertStatisticRepository advertStatisticRepository;

    private static final AtomicInteger EXTERNAL_IDS = new AtomicInteger(1_000_000);

    protected ClientEntity newClientWithToken() {
        ClientEntity client = newClient(uniqueChatId(), true);
        client.setToken("encrypted-token");
        client.increaseAvailableTestsCount(5);
        return clientRepository.save(client);
    }

    protected CardEntity newCard(ClientEntity owner) {
        CardEntity card = new CardEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setExternalId(EXTERNAL_IDS.incrementAndGet())
                .setExternalTitle("Card " + owner.getChatId())
                .setCategoryId(77)
                .setCategoryTitle("Category")
                .setBarcode("barcode-" + owner.getChatId())
                .setOwnerClient(owner)
                .setCardType(cardTypeRepository.findAll().get(0));
        return cardRepository.save(card);
    }

    protected AdvertEntity newAdvert(CardEntity card, AdvertStatus status) {
        AdvertEntity advert = new AdvertEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setExternalId(EXTERNAL_IDS.incrementAndGet())
                .setExternalTitle("Advert")
                .setChangeTime(new java.util.Date())
                .setStatus(status)
                .setOrdinalStatus(status.getCode())
                .setType(AdvertType.AUTO)
                .setOrdinalType(AdvertType.AUTO.getCode())
                .setCpm(100)
                .setStartBudgetSum(1000)
                .setStartCheckDateTime(LocalDateTime.now())
                .setCard(card);
        advert = advertRepository.save(advert);
        if (status == AdvertStatus.RUNNING) {
            countersStorage.add(advert.getExternalId());
        }
        return advert;
    }

    protected AdvertStatisticEntity newStatistic(AdvertEntity advert, ClientEntity client) {
        AdvertStatisticEntity statistic = new AdvertStatisticEntity()
                .setAdvertEntity(advert)
                .setClient(client)
                .setActive(true);
        return advertStatisticRepository.save(statistic);
    }

    protected SubscriptionEntity subscribe(ClientEntity client, LocalDateTime expiresAt) {
        return subscriptionRepository.save(
                new SubscriptionEntity().setClient(client).setExpiresAt(expiresAt));
    }
}
