package ru.akvine.wild.bot.infrastructure.session;

/**
 * Хранилище данных диалога клиента ({@code R}, на практике — {@link ClientSessionData}) по
 * идентификатору {@code ID} (chatId). Есть две реализации: {@link SessionStorageInMemoryImpl}
 * (быстрая, теряется при рестарте) и {@link SessionStorageInDatabaseImpl} (переживает рестарт
 * приложения).
 *
 * @param <ID> тип идентификатора клиента (на практике — {@link String}, chatId)
 * @param <R>  тип хранимых данных сессии
 */
public interface SessionStorage<ID, R> {
    /**
     * Заводит новую пустую сессию для идентификатора, если её ещё нет.
     *
     * @param identifier идентификатор клиента (chatId)
     */
    void init(ID identifier);

    /**
     * Возвращает данные сессии клиента.
     *
     * @param identifier идентификатор клиента (chatId)
     * @return данные сессии
     * @throws ru.akvine.wild.bot.infrastructure.exceptions.NoSessionException если сессии нет
     */
    R get(ID identifier);

    /**
     * Сохраняет изменённые данные сессии.
     *
     * @param data данные сессии для сохранения
     * @return сохранённые данные сессии
     */
    R save(R data);

    /**
     * Проверяет, есть ли сессия для идентификатора.
     *
     * @param identifier идентификатор клиента (chatId)
     * @return {@code true}, если сессия существует
     */
    boolean hasSession(ID identifier);

    /**
     * Удаляет сессию клиента.
     *
     * @param identifier идентификатор клиента (chatId)
     */
    void close(ID identifier);
}
