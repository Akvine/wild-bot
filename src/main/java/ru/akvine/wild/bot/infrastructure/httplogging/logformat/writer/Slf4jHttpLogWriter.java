package ru.akvine.wild.bot.infrastructure.httplogging.logformat.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import ru.akvine.wild.bot.infrastructure.httplogging.logformat.AbstractHttpMessageLogFormatter;
import ru.akvine.wild.bot.infrastructure.httplogging.logformat.RequestLogFormatter;
import ru.akvine.wild.bot.infrastructure.httplogging.logformat.ResponseLogFormatter;

/**
 * Пишет отформатированные запросы/ответы в slf4j-логгер с заданным именем и уровнем
 */
public class Slf4jHttpLogWriter extends AbstractHttpLogWriter {
    private final Logger logger;
    private final Level level;

    public Slf4jHttpLogWriter(RequestLogFormatter requestLogFormatter, ResponseLogFormatter responseLogFormatter) {
        this(requestLogFormatter, responseLogFormatter, null, null);
    }

    public Slf4jHttpLogWriter(AbstractHttpMessageLogFormatter abstractHttpMessageLogFormatter) {
        this(abstractHttpMessageLogFormatter, abstractHttpMessageLogFormatter);
    }

    public Slf4jHttpLogWriter(
            RequestLogFormatter requestLogFormatter,
            ResponseLogFormatter responseLogFormatter,
            String loggerName,
            Level level) {
        super(requestLogFormatter, responseLogFormatter);
        if (loggerName == null || loggerName.isEmpty()) {
            logger = LoggerFactory.getLogger(Slf4jHttpLogWriter.class);
        } else {
            logger = LoggerFactory.getLogger(loggerName);
        }

        this.level = level != null ? level : Level.DEBUG;
    }

    public Slf4jHttpLogWriter(AbstractHttpMessageLogFormatter abstractHttpMessageLogFormatter, String loggerName) {
        this(abstractHttpMessageLogFormatter, abstractHttpMessageLogFormatter, loggerName, null);
    }

    @Override
    public void log(String message) {
        switch (level) {
            case INFO:
                logger.info(message);
                break;
            case WARN:
                logger.warn(message);
                break;
            case DEBUG:
                logger.debug(message);
                break;
            case TRACE:
                logger.trace(message);
                break;
            case ERROR:
                logger.error(message);
                break;
            default:
                logger.debug(message);
        }
    }
}
