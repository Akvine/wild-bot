package ru.akvine.wild.bot.infrastructure.exceptions;

/**
 * Бросается реализациями {@code SessionStorage}, когда для запрошенного идентификатора
 * (chatId) нет сохранённой сессии диалога.
 */
public class NoSessionException extends RuntimeException {
    public NoSessionException(String message) {
        super(message);
    }
}
