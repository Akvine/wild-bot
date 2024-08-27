package ru.akvine.wild.bot.constants.telegram;

public final class TelegramMessageErrorConstants {
    private TelegramMessageErrorConstants() throws IllegalAccessException {
        throw new IllegalAccessException("Calling " + TelegramMessageErrorConstants.class.getSimpleName() + " constructor is prohibited!");
    }

    public static final String CLIENT_HAS_NO_SUBSCRIPTION_MESSAGE = "Вы не можете использовать функционал бота, т.к. нет подписки";
    public static final String CLIENT_SUBSCRIPTION_EXPIRED_MESSAGE = "Вы не можете использовать функционал бота, т.к. подписка истекла";
    public static final String CLIENT_NOT_IN_WHITELIST_MESSAGE = "Вы не можете использовать функционал бота, т.к. вы не в white-листе";
}
