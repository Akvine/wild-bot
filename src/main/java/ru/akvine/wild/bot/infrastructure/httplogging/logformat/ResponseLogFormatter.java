package ru.akvine.wild.bot.infrastructure.httplogging.logformat;

public interface ResponseLogFormatter {
    String format(ResponseLog responseLog);
}
