package ru.akvine.wild.bot.infrastructure.monitoring;

/**
 * Получает уведомления от JDBC-прокси ({@link MonitoringDataSourceProxy}) о выполнении запросов и
 * коммитах. Реализации вызываются в потоке, который работает с базой, поэтому должны быть быстрыми и
 * потокобезопасными и не должны бросать исключения.
 */
public interface SqlExecutionListener {

    /**
     * Запрос выполнен (успешно или с ошибкой).
     *
     * @param sql SQL-текст (для batch - все запросы пачки)
     * @param startedAtNanos значение {@link System#nanoTime()} на момент начала выполнения
     */
    void onQueryExecuted(String sql, long startedAtNanos);

    /**
     * Соединению выполнен явный {@code commit()}. Неявные коммиты в режиме autocommit не видны.
     */
    default void onCommit() {}
}
