package ru.akvine.wild.bot.bot;

import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;

/**
 * Финальное звено цепочки фильтров ({@code MessageFilter}), единое для всех платформ бота
 * (Telegram, MAX): принимает уже нормализованный {@link Payload} (не зависящий от конкретной
 * платформы) и решает, что с ним делать дальше — распознать текстовую команду вида
 * {@code /start}, обработать нажатие кнопки "Назад", либо передать данные резолверу текущего
 * состояния клиента ({@code ClientState}) из {@code StateStorage}. Единственная реализация —
 * {@link MessageDispatcherImpl}.
 */
public interface MessageDispatcher {
    /**
     * Обрабатывает один входящий payload и формирует ответ бота.
     *
     * @param payload нормализованные данные входящего сообщения/callback'а
     * @return ответ, готовый к отправке через API соответствующей платформы
     */
    Response doDispatch(Payload payload);
}
