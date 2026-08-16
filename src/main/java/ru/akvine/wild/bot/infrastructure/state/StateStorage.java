package ru.akvine.wild.bot.infrastructure.state;

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
     * @param state      новое текущее состояние
     */
    void add(T identifier, ClientState state);

    /**
     * Проверяет, есть ли для клиента сохранённая история состояний.
     *
     * @param identifier идентификатор клиента (chatId)
     * @return {@code true}, если история состояний существует
     */
    boolean containsState(T identifier);

    /**
     * Возвращает текущее (последнее в истории) состояние клиента.
     *
     * @param identifier идентификатор клиента (chatId)
     * @return текущее состояние
     * @throws ru.akvine.wild.bot.infrastructure.exceptions.NoStateException если истории нет
     */
    ClientState getCurrent(T identifier);

    /**
     * Убирает текущее (последнее) состояние из истории клиента.
     *
     * @param identifier идентификатор клиента (chatId)
     * @throws ru.akvine.wild.bot.infrastructure.exceptions.NoStateException если истории нет
     */
    void removeCurrent(T identifier);

    /**
     * Убирает текущее состояние из истории и возвращает новое текущее (то, что было
     * предыдущим) — используется для обработки кнопки "Назад".
     *
     * @param identifier идентификатор клиента (chatId)
     * @return состояние, ставшее текущим после удаления
     * @throws ru.akvine.wild.bot.infrastructure.exceptions.NoStateException если истории нет
     */
    ClientState removeCurrentAndGetPrevious(T identifier);

    /**
     * Полностью удаляет историю состояний клиента.
     *
     * @param identifier идентификатор клиента (chatId)
     * @throws ru.akvine.wild.bot.infrastructure.exceptions.NoStateException если истории нет
     */
    void close(T identifier);

    /**
     * Возвращает количество состояний в истории клиента.
     *
     * @param identifier идентификатор клиента (chatId)
     * @return размер истории состояний
     */
    int statesCount(T identifier);
}
