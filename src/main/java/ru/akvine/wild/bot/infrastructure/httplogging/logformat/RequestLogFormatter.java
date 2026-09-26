package ru.akvine.wild.bot.infrastructure.httplogging.logformat;

/**
 * Please be careful about thread safety
 */
public interface RequestLogFormatter {
    String format(RequestLog requestLog);
}
