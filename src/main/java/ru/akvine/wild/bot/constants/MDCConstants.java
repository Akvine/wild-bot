package ru.akvine.wild.bot.constants;

public final class MDCConstants {
    private MDCConstants() throws IllegalAccessException {
        throw new IllegalAccessException("Calling " + MDCConstants.class.getSimpleName() + " constructor is prohibited!");
    }

    public static final String USERNAME = "username";
    public static final String CHAT_ID = "chatId";
}
