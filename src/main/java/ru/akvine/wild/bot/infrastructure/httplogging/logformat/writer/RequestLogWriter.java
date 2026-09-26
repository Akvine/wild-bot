package ru.akvine.wild.bot.infrastructure.httplogging.logformat.writer;

import ru.akvine.wild.bot.infrastructure.httplogging.logformat.RequestLog;

public interface RequestLogWriter {
    void log(RequestLog requestLog);
}
