package ru.akvine.marketspace.bot.constants.telegram;

public final class TelegramMessageErrorConstants {
    private TelegramMessageErrorConstants() throws IllegalAccessException {
        throw new IllegalAccessException("Calling " + TelegramMessageErrorConstants.class.getSimpleName() + " constructor is prohibited!");
    }

    public static final String CLIENT_SUBSCRIPTION_MESSAGE = "Вы не можете использовать функционал бота, т.к. нет подписки или она истекла";
}
