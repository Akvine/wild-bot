package ru.akvine.wild.bot.services.integration.max;

import ru.akvine.wild.bot.services.integration.max.dto.LongPoolingSubscriptionResponse;
import ru.akvine.wild.bot.services.integration.max.dto.Update;

public interface MaxIntegrationService {
    /**
     * Метод получения обновления о событиях через Long Pooling
     *
     * Используется только в test / dev окружении
     * @return {@link LongPoolingSubscriptionResponse}
     */
    Update[] updates();
}
