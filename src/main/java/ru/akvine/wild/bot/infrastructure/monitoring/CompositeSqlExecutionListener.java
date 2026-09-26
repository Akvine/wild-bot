package ru.akvine.wild.bot.infrastructure.monitoring;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Передаёт события нескольким слушателям. Сбой одного слушателя не мешает остальным и не ломает
 * работу с базой.
 */
@Slf4j
public class CompositeSqlExecutionListener implements SqlExecutionListener {
    private final List<SqlExecutionListener> listeners;

    public CompositeSqlExecutionListener(List<SqlExecutionListener> listeners) {
        this.listeners = List.copyOf(listeners);
    }

    @Override
    public void onQueryExecuted(String sql, long startedAtNanos) {
        for (SqlExecutionListener listener : listeners) {
            try {
                listener.onQueryExecuted(sql, startedAtNanos);
            } catch (RuntimeException e) {
                logger.error("SQL execution listener [{}] failed", listener.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public void onCommit() {
        for (SqlExecutionListener listener : listeners) {
            try {
                listener.onCommit();
            } catch (RuntimeException e) {
                logger.error("SQL execution listener [{}] failed", listener.getClass().getSimpleName(), e);
            }
        }
    }
}
