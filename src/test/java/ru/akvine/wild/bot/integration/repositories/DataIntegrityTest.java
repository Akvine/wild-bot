package ru.akvine.wild.bot.integration.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.akvine.wild.bot.entities.CardEntity;
import ru.akvine.wild.bot.entities.CardTypeEntity;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.entities.SubscriptionEntity;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.integration.base.BaseTest;
import ru.akvine.wild.bot.repositories.CardRepository;
import ru.akvine.wild.bot.repositories.CardTypeRepository;
import ru.akvine.wild.bot.repositories.ClientRepository;
import ru.akvine.wild.bot.repositories.SubscriptionRepository;
import ru.akvine.wild.bot.utils.UUIDGenerator;

@Transactional
class DataIntegrityTest extends BaseTest {
    private static final long MISSING_CLIENT_ID = 987_654_321L;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CardTypeRepository cardTypeRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ClientEntity newClient() {
        return new ClientEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setChatId("chat-" + UUIDGenerator.uuidWithoutDashes())
                .setFirstName("Test")
                .setBotType(BotType.TELEGRAM);
    }

    private CardEntity newCard(ClientEntity owner) {
        CardTypeEntity cardType =
                cardTypeRepository.save(new CardTypeEntity().setType("type-" + UUIDGenerator.uuidWithoutDashes()));
        return new CardEntity()
                .setUuid(UUIDGenerator.uuidWithoutDashes())
                .setExternalId((int) (Math.random() * 1_000_000))
                .setExternalTitle("card")
                .setCategoryId(1)
                .setCategoryTitle("category")
                .setBarcode("barcode-" + UUIDGenerator.uuidWithoutDashes())
                .setOwnerClient(owner)
                .setCardType(cardType);
    }

    @Test
    void card_withUnknownClient_isRejected() {
        ClientEntity missing = new ClientEntity().setId(MISSING_CLIENT_ID);

        assertThatThrownBy(() -> cardRepository.saveAndFlush(newCard(missing)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void card_withExistingClient_isSaved() {
        ClientEntity client = clientRepository.save(newClient());

        assertThatCode(() -> cardRepository.saveAndFlush(newCard(client))).doesNotThrowAnyException();
    }

    @Test
    void subscription_withUnknownClient_isRejected() {
        ClientEntity missing = new ClientEntity().setId(MISSING_CLIENT_ID);
        SubscriptionEntity subscription = new SubscriptionEntity()
                .setClient(missing)
                .setExpiresAt(LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> subscriptionRepository.saveAndFlush(subscription))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void markDeleted_setsFlagAndDateTogether_andIsAccepted() {
        ClientEntity client = clientRepository.save(newClient());

        client.markDeleted();
        clientRepository.saveAndFlush(client);

        assertThat(client.isDeleted()).isTrue();
        assertThat(client.getDeletedDate()).isNotNull();
    }

    @Test
    void restore_clearsFlagAndDateTogether_andIsAccepted() {
        ClientEntity client = newClient();
        client.markDeleted();
        clientRepository.saveAndFlush(client);

        client.restore();
        clientRepository.saveAndFlush(client);

        assertThat(client.isDeleted()).isFalse();
        assertThat(client.getDeletedDate()).isNull();
    }

    @Test
    void flagWithoutDate_isRejectedByDatabase() {
        ClientEntity client = clientRepository.saveAndFlush(newClient());

        assertThatThrownBy(() ->
                        jdbcTemplate.update("update client_entity set is_deleted = true where id = ?", client.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void dateWithoutFlag_isRejectedByDatabase() {
        ClientEntity client = clientRepository.saveAndFlush(newClient());

        assertThatThrownBy(() -> jdbcTemplate.update(
                        "update client_entity set deleted_date = current_timestamp where id = ?", client.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
