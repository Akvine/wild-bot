package ru.akvine.wild.bot.integration.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.akvine.wild.bot.entities.AdvertEntity;
import ru.akvine.wild.bot.entities.AdvertStatisticEntity;
import ru.akvine.wild.bot.entities.CardEntity;
import ru.akvine.wild.bot.entities.CardTypeEntity;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.enums.AdvertStatus;
import ru.akvine.wild.bot.enums.AdvertType;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.integration.base.BaseTest;
import ru.akvine.wild.bot.repositories.AdvertRepository;
import ru.akvine.wild.bot.repositories.AdvertStatisticRepository;
import ru.akvine.wild.bot.repositories.CardRepository;
import ru.akvine.wild.bot.repositories.CardTypeRepository;
import ru.akvine.wild.bot.repositories.ClientRepository;
import ru.akvine.wild.bot.utils.UUIDGenerator;

@Transactional
class AdvertStatisticRepositoryTest extends BaseTest {
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CardTypeRepository cardTypeRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private AdvertRepository advertRepository;

    @Autowired
    private AdvertStatisticRepository advertStatisticRepository;

    private final AtomicInteger externalIdSequence = new AtomicInteger();

    private AdvertEntity saveAdvertWithStatistic() {
        int externalId = externalIdSequence.incrementAndGet();
        ClientEntity client = clientRepository.save(new ClientEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setChatId("chat-" + UUIDGenerator.uuidWithoutDashes())
                .setFirstName("Test")
                .setBotType(BotType.TELEGRAM));

        CardTypeEntity cardType =
                cardTypeRepository.save(new CardTypeEntity().setType("type-" + UUIDGenerator.uuidWithoutDashes()));

        CardEntity card = cardRepository.save(new CardEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setExternalId(externalId)
                .setExternalTitle("card")
                .setCategoryId(1)
                .setCategoryTitle("category")
                .setBarcode("barcode-" + externalId)
                .setOwnerClient(client)
                .setCardType(cardType));

        AdvertEntity advert = advertRepository.save(new AdvertEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setExternalId(externalId)
                .setExternalTitle("advert")
                .setChangeTime(new Date())
                .setStatus(AdvertStatus.PAUSE)
                .setOrdinalStatus(AdvertStatus.PAUSE.getCode())
                .setType(AdvertType.AUTO)
                .setOrdinalType(AdvertType.AUTO.getCode())
                .setCpm(100)
                .setCard(card));

        advertStatisticRepository.save(new AdvertStatisticEntity()
                .setAdvertEntity(advert)
                .setClient(client)
                .setActive(false));

        return advert;
    }

    @Test
    void deleteByAdvertIds_actuallyDeletesStatistics_insteadOfThrowing() {
        AdvertEntity advert = saveAdvertWithStatistic();

        assertThatCode(() -> advertStatisticRepository.deleteByAdvertIds(List.of(advert.getId())))
                .doesNotThrowAnyException();

        assertThat(advertStatisticRepository.findByClientId(
                        advert.getCard().getOwnerClient().getId()))
                .isEmpty();
    }

    @Test
    void deleteByAdvertIds_leavesStatisticsOfOtherAdverts() {
        AdvertEntity keep = saveAdvertWithStatistic();
        AdvertEntity remove = saveAdvertWithStatistic();

        advertStatisticRepository.deleteByAdvertIds(List.of(remove.getId()));

        assertThat(advertStatisticRepository.findByClientId(
                        keep.getCard().getOwnerClient().getId()))
                .hasSize(1);
    }
}
