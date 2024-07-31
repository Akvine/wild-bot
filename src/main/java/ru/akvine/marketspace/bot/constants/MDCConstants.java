package ru.akvine.marketspace.bot.constants;

public final class MDCConstants {
    private MDCConstants() throws IllegalAccessException {
        throw new IllegalAccessException("Calling " + MDCConstants.class.getName() + " constructor is prohibited!");
    }

    public static final String USERNAME = "username";
    public static final String CHAT_ID = "chatId";
}
