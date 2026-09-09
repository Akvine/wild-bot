package ru.akvine.wild.bot.constants;

public final class LockConstants {
    private LockConstants() throws IllegalAccessException {
        throw new IllegalAccessException(
                "Calling " + LockConstants.class.getSimpleName() + " constructor is prohibited!");
    }

    public static final String UPLOAD_PHOTO_LOCK = "UPLOAD_CARD_PHOTO_STATE_";

    public static final String ACCESS_RESTORE_PREFIX = "ACCESS_RESTORE_";
    public static final String AUTH_PREFIX = "AUTH_";
    public static final String REG_PREFIX = "REG_";
    public static final String CLIENT_LOCK_ID_PREFIX = "CLIENT_";
}
