package ru.akvine.marketspace.bot.exceptions.handler;

public class CommonErrorCodes {
    private CommonErrorCodes() {throw new IllegalStateException("CommonErrorCodes can't be called!");}

    public final static String GENERAL_ERROR = "general.error";

    public final static String BLOCKED_ERROR = "blocked.error";

    public final static String ADVERT_ALREADY_IN_PAUSE_STATE_ERROR = "advert.already.inPause.state.error";

    public interface Validation {
        String BOTH_PARAMETERS_PRESENT_ERROR = "both.parameters.present.error";
        String BOTH_PARAMETERS_BLANK_ERROR = "both.parameters.blank.error";

        String ADVERT_STATUS_BLANK_ERROR = "advert.status.blank.error";
        String ADVERT_STATUS_INVALID_ERROR = "advert.status.invalid.error";
    }
}
