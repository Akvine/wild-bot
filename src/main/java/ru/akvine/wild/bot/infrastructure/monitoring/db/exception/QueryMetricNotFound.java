package ru.akvine.wild.bot.infrastructure.monitoring.db.exception;

public class QueryMetricNotFound extends DbMetricsException {
    public QueryMetricNotFound(String threadMnemonic, String queryMnemonic) {
        super("Query metric [" + queryMnemonic + "] of thread metric [" + threadMnemonic + "] not found");
    }
}
