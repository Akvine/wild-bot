package ru.akvine.wild.bot.infrastructure.monitoring.db;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Что писать в лог при коммитах; флаги можно менять на лету
 */
public class DbMetricsLoggingConfig {
    private final AtomicBoolean commitLoggingEnabled = new AtomicBoolean(false);
    private final AtomicBoolean commitStackTraceLoggingEnabled = new AtomicBoolean(false);

    public DbMetricsLoggingConfig setCommitLoggingEnabled(boolean commitLoggingEnabled) {
        this.commitLoggingEnabled.set(commitLoggingEnabled);
        return this;
    }

    public DbMetricsLoggingConfig setCommitStackTraceLoggingEnabled(boolean commitStackTraceLoggingEnabled) {
        this.commitStackTraceLoggingEnabled.set(commitStackTraceLoggingEnabled);
        return this;
    }

    public boolean isCommitLoggingEnabled() {
        return commitLoggingEnabled.get();
    }

    public boolean isCommitStackTraceLoggingEnabled() {
        return commitStackTraceLoggingEnabled.get();
    }
}
