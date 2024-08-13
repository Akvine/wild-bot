package ru.akvine.marketspace.bot.constants;

public final class ApiErrorConstants {
    private ApiErrorConstants() throws IllegalAccessException {throw new IllegalAccessException("Calling " + ApiErrorConstants.class.getSimpleName() + " constructor is prohibited!");}

    public final static String GENERAL_ERROR = "general.error";

    public final static String BLOCKED_ERROR = "blocked.error";

    public final static String ADVERT_ALREADY_IN_PAUSE_STATE_ERROR = "advert.already.inPause.state.error";

    public final static String SUBSCRIPTION_NOT_FOUND_ERROR = "subscription.notFound.error";

    public interface Validation {
        String BOTH_PARAMETERS_PRESENT_ERROR = "both.parameters.present.error";
        String BOTH_PARAMETERS_BLANK_ERROR = "both.parameters.blank.error";

        String ADVERT_STATUS_BLANK_ERROR = "advert.status.blank.error";
        String ADVERT_STATUS_INVALID_ERROR = "advert.status.invalid.error";

        String MAX_CLIENTS_SEND_MESSAGE_COUNT_ERROR = "max.clients.sendMessage.count.error";

        String LESS_THEN_MIN_VALUE_ERROR = "less.than.min.value.error";
    }
}
