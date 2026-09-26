package ru.akvine.wild.bot.infrastructure.http.log;

/**
 * Настройки {@link LoggingInterceptor}
 */
public class LoggingInterceptorExtensions {
    private boolean logRequestInDebug;
    private boolean logResponseInDebug;
    private boolean logResponseStatus;

    public boolean isLogRequestInDebug() {
        return logRequestInDebug;
    }

    public boolean isLogResponseInDebug() {
        return logResponseInDebug;
    }

    public boolean isLogResponseStatus() {
        return logResponseStatus;
    }

    /** {@code true} - запрос пишется на DEBUG, {@code false} - на TRACE */
    public LoggingInterceptorExtensions logRequestInDebug(boolean logRequestInDebug) {
        this.logRequestInDebug = logRequestInDebug;
        return this;
    }

    /** {@code true} - ответ пишется на DEBUG, {@code false} - на TRACE */
    public LoggingInterceptorExtensions logResponseInDebug(boolean logResponseInDebug) {
        this.logResponseInDebug = logResponseInDebug;
        return this;
    }

    /** добавлять к телу ответа статус, если он не 200 */
    public LoggingInterceptorExtensions logResponseStatus(boolean logResponseStatus) {
        this.logResponseStatus = logResponseStatus;
        return this;
    }
}
