package ru.akvine.wild.bot.services.integration.max;

import ru.akvine.wild.bot.services.integration.max.dto.Message;
import ru.akvine.wild.bot.services.integration.max.dto.request.SendMessageRequest;
import ru.akvine.wild.bot.services.integration.max.dto.response.LongPoolingSubscriptionResponse;
import ru.akvine.wild.bot.services.integration.max.dto.Update;

public interface MaxIntegrationService {
    /**
     * Метод получения обновления о событиях через Long Pooling
     *
     * Используется только в test / dev окружении
     * @return {@link LongPoolingSubscriptionResponse}
     */
    Update[] updates();

    /**
     * Метод получения сообщений из chatId
     *
     * @param chatId уникальный идентификатор чата в боте
     * @return список сообщений
     */
    Message[] getMessages(String chatId);

    /**
     * Метод по отправке сообщений
     */
    void sendMessage(String chatId, SendMessageRequest request);
}
