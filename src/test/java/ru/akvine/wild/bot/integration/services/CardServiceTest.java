package ru.akvine.wild.bot.integration.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.akvine.wild.bot.entities.CardEntity;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.exceptions.CardNotFoundException;
import ru.akvine.wild.bot.exceptions.CardTypeNotFoundException;
import ru.akvine.wild.bot.integration.base.BaseTest;
import ru.akvine.wild.bot.repositories.CardRepository;
import ru.akvine.wild.bot.repositories.ClientRepository;
import ru.akvine.wild.bot.services.CardService;
import ru.akvine.wild.bot.services.domain.CardModel;
import ru.akvine.wild.bot.services.dto.admin.card.ListCards;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.CardCharacteristic;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.CardDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.SizeDto;
import ru.akvine.wild.bot.utils.UUIDGenerator;

@DisplayName("CardService: создание карточек из ответа Wildberries, поиск и списки")
class CardServiceTest extends BaseTest {
    private static final AtomicInteger IDS = new AtomicInteger(9_000_000);

    @Autowired
    private CardService cardService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private ClientRepository clientRepository;

    private ClientEntity client() {
        String chatId = String.valueOf(ThreadLocalRandom.current().nextLong(1_000_000_000L, Long.MAX_VALUE));
        return clientRepository.save(new ClientEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setChatId(chatId)
                .setUsername("u" + chatId)
                .setFirstName("First")
                .setBotType(BotType.TELEGRAM)
                .setInWhitelist(true));
    }

    private static CardDto dto(int nmId, int subjectId, String type, List<SizeDto> sizes) {
        return new CardDto()
                .setNmID(nmId)
                .setTitle("Title " + nmId)
                .setSubjectID(subjectId)
                .setSubjectName("Subject " + subjectId)
                .setSizes(sizes)
                .setCharacteristics(List.of(
                        new CardCharacteristic().setName("Цвет").setValue(List.of("Красный")),
                        new CardCharacteristic().setName("Пол").setValue(List.of(type))));
    }

    private static List<SizeDto> oneSize(String barcode) {
        return List.of(new SizeDto().setSkus(List.of(barcode)));
    }

    private CardModel createCard(ClientEntity owner, int categoryId, String type) {
        int nmId = IDS.incrementAndGet();
        return cardService
                .create(List.of(dto(nmId, categoryId, type, oneSize("barcode-" + nmId))), owner.getUuid())
                .getFirst();
    }

    @Test
    @DisplayName("Создание: карточка сохраняется с владельцем, штрихкодом и типом из характеристики «Пол»")
    void createSavesCard() {
        ClientEntity owner = client();
        int nmId = IDS.incrementAndGet();

        List<CardModel> created =
                cardService.create(List.of(dto(nmId, 777, "Женский", oneSize("bc-1"))), owner.getUuid());

        assertThat(created).hasSize(1);
        CardEntity saved = cardRepository.findByExternalId(nmId).orElseThrow();
        assertThat(saved.getBarcode()).isEqualTo("bc-1");
        assertThat(saved.getCategoryId()).isEqualTo(777);
        assertThat(saved.getExternalTitle()).isEqualTo("Title " + nmId);
        assertThat(created.getFirst().getCardType().getType()).isEqualTo("Женский");
    }

    @Test
    @DisplayName(
            "Создание: несколько размеров или штрихкодов, нет характеристики «Пол», неизвестный тип и владелец - ошибки")
    void createValidation() {
        ClientEntity owner = client();
        int nmId = IDS.incrementAndGet();

        List<SizeDto> twoSizes = List.of(new SizeDto().setSkus(List.of("a")), new SizeDto().setSkus(List.of("b")));
        assertThatThrownBy(() -> cardService.create(List.of(dto(nmId, 1, "Мужской", twoSizes)), owner.getUuid()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("more 1 sizes");

        List<SizeDto> twoBarcodes = List.of(new SizeDto().setSkus(List.of("a", "b")));
        assertThatThrownBy(() -> cardService.create(List.of(dto(nmId, 1, "Мужской", twoBarcodes)), owner.getUuid()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("more 1 barcodes");

        CardDto withoutGender = dto(nmId, 1, "Мужской", oneSize("x")).setCharacteristics(List.of());
        assertThatThrownBy(() -> cardService.create(List.of(withoutGender), owner.getUuid()))
                .isInstanceOf(CardTypeNotFoundException.class)
                .hasMessageContaining("Пол");

        assertThatThrownBy(
                        () -> cardService.create(List.of(dto(nmId, 1, "Неизвестный", oneSize("x"))), owner.getUuid()))
                .isInstanceOf(CardTypeNotFoundException.class)
                .hasMessageContaining("Неизвестный");

        assertThatThrownBy(() -> cardService.create(List.of(dto(nmId, 1, "Мужской", oneSize("x"))), "no-such-client"))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> cardService.create(null, owner.getUuid())).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Поиск по внешнему id: найденная карточка и ошибка для отсутствующей")
    void verifyExistsByExternalId() {
        CardModel created = createCard(client(), 801, "Мужской");

        assertThat(cardService.verifyExistsByExternalId(created.getExternalId()).getExternalId())
                .isEqualTo(created.getExternalId());
        assertThatThrownBy(() -> cardService.verifyExistsByExternalId(-424242))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    @DisplayName("Карточки по категории: список, первая и ошибка, когда в категории пусто")
    void byCategory() {
        ClientEntity owner = client();
        int category = 810 + IDS.incrementAndGet() % 1000;
        createCard(owner, category, "Мужской");
        createCard(owner, category, "Мужской");

        assertThat(cardService.getByCategoryId(category)).hasSize(2);
        assertThat(cardService.getFirst(category).getCategoryId()).isEqualTo(category);
        assertThatThrownBy(() -> cardService.getFirst(-999_999)).isInstanceOf(CardNotFoundException.class);
        assertThat(cardService.getByCategoryId(-999_999)).isEmpty();
    }

    @Test
    @DisplayName("Карточки по типу и по клиенту (chatId + тип бота)")
    void byTypeAndByClient() {
        ClientEntity owner = client();
        CardModel female = createCard(owner, 820, "Женский");

        assertThat(cardService.getByType("Женский"))
                .extracting(CardModel::getExternalId)
                .contains(female.getExternalId());
        assertThat(cardService.getByChatIdAndBotType(owner.getChatId(), BotType.TELEGRAM))
                .extracting(CardModel::getExternalId)
                .containsExactly(female.getExternalId());
        assertThat(cardService.getByChatIdAndBotType("nobody", BotType.TELEGRAM))
                .isEmpty();
    }

    @Test
    @DisplayName("Список для админки: фильтры по названию, id, названию категории и категории; постраничность")
    void listWithFilters() {
        ClientEntity owner = client();
        CardModel first = createCard(owner, 830, "Мужской");
        createCard(owner, 830, "Мужской");
        createCard(owner, 831, "Мужской");

        assertThat(cardService.list(
                        new ListCards().setExternalId(first.getExternalId()).setCount(10)))
                .extracting(CardModel::getExternalId)
                .containsExactly(first.getExternalId());
        assertThat(cardService.list(new ListCards()
                        .setExternalTitle("Title " + first.getExternalId())
                        .setCount(10)))
                .hasSize(1);
        assertThat(cardService.list(new ListCards().setCategoryId(830).setCount(10)))
                .hasSize(2);
        assertThat(cardService.list(new ListCards().setCategoryId(830).setCount(1)))
                .hasSize(1);
        assertThat(cardService.list(
                        new ListCards().setCategoryTitle("Subject 831").setCount(10)))
                .hasSize(1);
        assertThat(cardService.list(new ListCards().setCount(5))).isNotEmpty();
        assertThat(cardService.list(new ListCards().setCategoryId(-1).setCount(10)))
                .isEmpty();
    }
}
