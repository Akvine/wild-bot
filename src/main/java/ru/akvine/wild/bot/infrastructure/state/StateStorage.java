package ru.akvine.wild.bot.infrastructure.state;

import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;

/**
 * Стек состояний диалога клиента, по идентификатору {@code T} (chatId) — не просто текущее
 * состояние, а вся история переходов, что и позволяет кнопке "Назад" возвращать клиента на
 * предыдущий экран ({@link #removeCurrentAndGetPrevious}). Единственная реализация —
 * {@link StateStorageInMemoryImpl}.
 *
 * @param <T> тип идентификатора клиента (на практике — {@link String}, chatId)
 * @param <R> тип хранимой истории состояний (на практике — {@code List<ClientState>})
 */
public interface StateStorage<T, R> {
    /**
     * Добавляет новое состояние на вершину стека состояний клиента, делая его текущим.
     * Если истории ещё нет — создаёт её с этим состоянием в качестве первого элемента.
     *
     * @param identifier идентификатор клиента (chatId)
     * @param botType тип бота (Telegram или Max)
     * @param state      новое текущее состояние
     */
    void add(T identifier, BotType botType, ClientState state);

    /**
     * Проверяет, есть ли для клиента сохранённая история состояний.
     *
     * @param identifier идентификатор клиента (chatId)
     * @param botType тип бота (Telegram или Max)
     * @return {@code true}, если история состояний существует
     */
    boolean containsState(T identifier, BotType botType);

    /**
     * Возвращает текущее (последнее в истории) состояние клиента.
     *
     * @param identifier идентификатор клиента (chatId)
     * @param botType тип бота (Telegram или Max)
     * @return текущее состояние
     * @throws ru.akvine.wild.bot.infrastructure.exceptions.NoStateException если истории нет
     */
    ClientState getCurrent(T identifier, BotType botType);

    /**
     * Убирает текущее (последнее) состояние из истории клиента.
     *
     * @param identifier идентификатор клиента (chatId)
     * @param botType тип бота (Telegram или Max)
     * @throws ru.akvine.wild.bot.infrastructure.exceptions.NoStateException если истории нет
     */
    void removeCurrent(T identifier, BotType botType);

    /**
     * Убирает текущее состояние из истории и возвращает новое текущее (то, что было
     * предыдущим) — используется для обработки кнопки "Назад".
     *
     * @param identifier идентификатор клиента (chatId)
     * @param botType тип бота (Telegram или Max)
     * @return состояние, ставшее текущим после удаления
     * @throws ru.akvine.wild.bot.infrastructure.exceptions.NoStateException если истории нет
     */
    ClientState removeCurrentAndGetPrevious(T identifier, BotType botType);

    /**
     * Выполняет откат состояния клиента к указанному целевому состоянию.
     * <p>
     * Метод формирует уникальный идентификатор на основе идентификатора чата и типа бота,
     * затем ищет соответствующую запись о состояниях клиента. Если запись найдена и в её списке
     * состояний присутствует целевое состояние {@code targetClientState}, список состояний
     * обрезается до этого состояния включительно, обновляется дата последнего изменения и
     * изменения сохраняются в хранилище. После успешного выполнения возвращается {@code true}.
     * <p>
     * Если запись о состояниях для заданных параметров не найдена или целевое состояние
     * отсутствует в списке, метод не вносит изменений и возвращает {@code false}.
     *
     * @param identifier        идентификатор чата, для которого выполняется откат состояния
     * @param botType           тип бота, определяющий контекст обработки состояний
     * @param targetClientState целевое состояние клиента, к которому необходимо откатить
     *                          текущую последовательность состояний
     * @return {@code true}, если откат выполнен успешно и состояние обновлено;
     *         {@code false}, если запись не найдена или целевое состояние отсутствует в списке
     */
    boolean backAt(T identifier, BotType botType, ClientState targetClientState);

    /**
     * Полностью удаляет историю состояний клиента.
     *
     * @param identifier идентификатор клиента (chatId)
     * @param botType тип бота (Telegram или Max)
     * @throws ru.akvine.wild.bot.infrastructure.exceptions.NoStateException если истории нет
     */
    void close(T identifier, BotType botType);

    /**
     * Возвращает количество состояний в истории клиента.
     *
     * @param identifier идентификатор клиента (chatId)
     * @param botType тип бота (Telegram или Max)
     * @return размер истории состояний
     */
    int statesCount(T identifier, BotType botType);
}
