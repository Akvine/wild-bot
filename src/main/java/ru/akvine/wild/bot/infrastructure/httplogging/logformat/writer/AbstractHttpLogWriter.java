package ru.akvine.wild.bot.infrastructure.httplogging.logformat.writer;

import ru.akvine.wild.bot.infrastructure.httplogging.logformat.AbstractHttpMessageLogFormatter;
import ru.akvine.wild.bot.infrastructure.httplogging.logformat.RequestLog;
import ru.akvine.wild.bot.infrastructure.httplogging.logformat.RequestLogFormatter;
import ru.akvine.wild.bot.infrastructure.httplogging.logformat.ResponseLog;
import ru.akvine.wild.bot.infrastructure.httplogging.logformat.ResponseLogFormatter;

public abstract class AbstractHttpLogWriter implements RequestLogWriter, ResponseLogWriter {
    private final RequestLogFormatter requestLogFormatter;
    private final ResponseLogFormatter responseLogFormatter;

    public AbstractHttpLogWriter(RequestLogFormatter requestLogFormatter, ResponseLogFormatter responseLogFormatter) {
        this.requestLogFormatter = requestLogFormatter;
        this.responseLogFormatter = responseLogFormatter;
    }

    public AbstractHttpLogWriter(AbstractHttpMessageLogFormatter abstractHttpMessageLogFormatter) {
        this(abstractHttpMessageLogFormatter, abstractHttpMessageLogFormatter);
    }

    @Override
    public final void log(RequestLog requestLog) {
        String formattedMessage = requestLogFormatter.format(requestLog);
        log(formattedMessage);
    }

    @Override
    public final void log(ResponseLog responseLog) {
        String formattedMessage = responseLogFormatter.format(responseLog);
        log(formattedMessage);
    }

    abstract void log(String message);
}
