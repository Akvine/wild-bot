package ru.akvine.wild.bot.infrastructure.outbox;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Точка входа transactional outbox. Вызывается внутри транзакции бизнес-изменения: сообщение
 * записывается в ту же БД той же транзакцией, поэтому оно появится тогда и только тогда, когда изменение
 * зафиксировано, и не потеряется, если отправка во внешнюю систему упадёт. Реальную отправку и повторы
 * делает {@link OutboxRelay}.
 * <pre>
 * transactionTemplate.executeWithoutResult(status -> {
 *     subscription.setNotifiedThatExpires(true);
 *     subscriptionRepository.save(subscription);
 *     outboxService.enqueue("BOT_MESSAGE", payload, "subscription-expiring:" + subscription.getId());
 * });
 * </pre>
 */
@Slf4j
public class OutboxService {
    private final OutboxStore store;
    private final JsonMapper mapper = JsonMapper.builder()
            .findAndAddModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public OutboxService(OutboxStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    /**
     * Добавляет сообщение в очередь на отправку
     *
     * @param type тип сообщения: его доставляет {@link OutboxHandler} с таким же {@code type()}
     * @param payload содержимое, сериализуется в JSON
     * @param dedupKey ключ дедупликации (например, {@code subscription-expiring:42}): второе сообщение с тем
     *        же ключом не добавляется; {@code null} - без дедупликации
     * @return {@code true}, если сообщение добавлено, {@code false}, если такой ключ уже был
     * @throws org.springframework.transaction.IllegalTransactionStateException если вызвано вне транзакции
     */
    public boolean enqueue(String type, Object payload, String dedupKey) {
        Objects.requireNonNull(type, "type");
        boolean added = store.add(type, toJson(payload), dedupKey);
        if (!added) {
            logger.info("Outbox message [{}] with dedup key [{}] already exists, skipped", type, dedupKey);
        }
        return added;
    }

    /**
     * Разбирает содержимое сообщения; используется в {@link OutboxHandler}
     */
    public <T> T read(OutboxMessage message, Class<T> payloadType) {
        try {
            return mapper.readValue(message.payload(), payloadType);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Can't read outbox message #" + message.id() + " as " + payloadType.getSimpleName(), e);
        }
    }

    private String toJson(Object payload) {
        try {
            return mapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't serialize outbox payload " + payload.getClass().getSimpleName(), e);
        }
    }
}
