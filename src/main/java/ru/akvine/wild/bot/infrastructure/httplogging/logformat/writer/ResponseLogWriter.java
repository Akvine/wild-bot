package ru.akvine.wild.bot.infrastructure.httplogging.logformat.writer;

import ru.akvine.wild.bot.infrastructure.httplogging.logformat.ResponseLog;

public interface ResponseLogWriter {
    void log(ResponseLog responseLog);
}
