package ru.akvine.wild.bot.enums;

public enum AdvertStatus {
    IN_REMOVAL(-1),
    READY_FOR_START(4),
    COMPLETED(7),
    RUNNING(9),
    PAUSE(11);

    private final int code;

    AdvertStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static AdvertStatus getByCode(int code) {
        for (AdvertStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("No Status with code " + code + " found");
    }
}
