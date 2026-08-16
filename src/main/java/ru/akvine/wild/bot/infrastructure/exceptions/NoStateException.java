package ru.akvine.wild.bot.infrastructure.exceptions;

/**
 * Бросается реализациями {@code StateStorage}, когда для запрошенного идентификатора
 * (chatId) нет сохранённого текущего состояния диалога.
 */
public class NoStateException extends RuntimeException {
    public NoStateException(String message) {
        super(message);
    }
}
