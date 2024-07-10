package ru.akvine.marketspace.bot.constants;

public final class TelegramMessageConstants {
    private TelegramMessageConstants() {
        throw new IllegalStateException("Calling TelegramMessageConstants constructor is prohibited!");
    }

    public static final String DEFAULT_MESSAGE = "Введите команду: ";
    public static final String UNKNOWN_COMMAND_MESSAGE = "Неизвестная команда. Введите /help для просмотра списка доступных команд";
}
