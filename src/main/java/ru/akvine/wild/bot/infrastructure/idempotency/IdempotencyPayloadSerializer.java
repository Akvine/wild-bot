package ru.akvine.wild.bot.infrastructure.idempotency;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Переводит результат операции и записи хранилища в JSON и обратно. Используется собственный маппер,
 * не зависящий от настроек Jackson в приложении: даты пишутся строками ISO, неизвестные поля при
 * чтении игнорируются.
 */
public class IdempotencyPayloadSerializer {
    private final JsonMapper mapper = JsonMapper.builder()
            .findAndAddModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public String toJson(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Can't serialize " + value.getClass().getSimpleName(), e);
        }
    }

    public <T> T fromJson(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("Can't deserialize " + type.getSimpleName(), e);
        }
    }
}
