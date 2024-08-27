package ru.akvine.marketspace.bot.constants;

public final class DbLockConstants {
    private DbLockConstants() throws IllegalAccessException {
        throw new IllegalAccessException("Calling " + DbLockConstants.class.getSimpleName() + " constructor is prohibited!");
    }

    public static final String UPLOAD_PHOTO_LOCK = "UPLOAD_CARD_PHOTO_STATE_";
}
