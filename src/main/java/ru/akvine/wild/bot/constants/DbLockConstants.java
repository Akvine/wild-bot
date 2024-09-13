package ru.akvine.wild.bot.constants;

public final class DbLockConstants {
    private DbLockConstants() throws IllegalAccessException {
        throw new IllegalAccessException("Calling " + DbLockConstants.class.getSimpleName() + " constructor is prohibited!");
    }

    public static final String UPLOAD_PHOTO_LOCK = "UPLOAD_CARD_PHOTO_STATE_";

    public static final String DEFAULT_PROCESS_DB_LOCK = "LOCK_by_DB";
}
