package ru.akvine.wild.bot.constants;

public final class ApiErrorConstants {
    private ApiErrorConstants() throws IllegalAccessException {
        throw new IllegalAccessException("Calling " + ApiErrorConstants.class.getSimpleName() + " constructor is prohibited!");
    }

    public final static String NO_SESSION_ERROR = "no.session.error";

    public final static String GENERAL_ERROR = "general.error";

    public final static String BLOCKED_ERROR = "blocked.error";

    public final static String ADVERT_ALREADY_IN_PAUSE_STATE_ERROR = "advert.already.inPause.state.error";

    public final static String CLIENT_NOT_FOUND_ERROR = "client.notFound.error";

    public final static String RESOURCE_NOT_FOUND_ERROR = "resource.notFound.error";
    public final static String JSON_BODY_INVALID_ERROR = "json.body.invalid.error";

    public interface Validation {
        String BOTH_PARAMETERS_PRESENT_ERROR = "both.parameters.present.error";
        String BOTH_PARAMETERS_BLANK_ERROR = "both.parameters.blank.error";
        String FIELD_NOT_PRESENTED_ERROR = "field.not.presented.error";

        String BAD_CREDENTIALS_ERROR = "bad.credentials.error";

        String ADVERT_STATUS_BLANK_ERROR = "advert.status.blank.error";
        String ADVERT_STATUS_INVALID_ERROR = "advert.status.invalid.error";

        String MAX_CLIENTS_SEND_MESSAGE_COUNT_ERROR = "max.clients.sendMessage.count.error";

        String EMAIL_BLANK_ERROR = "email.blank.error";
        String EMAIL_INVALID_ERROR = "email.invalid.error";

        String LESS_THEN_MIN_VALUE_ERROR = "less.than.min.value.error";

        String REGISTRATION_PASSWORD_BLANK_ERROR = "registration.password.blank.error";
        String REGISTRATION_PASSWORD_INVALID_ERROR = "registration.password.invalid.error";
    }

    public interface Security {
        String INVALID_ATTEMPT_ERROR = "invalid.attempt.error";
        String INVALID_SESSION_ERROR = "invalid.session.error";
        String ACTION_NOT_STARTED_ERROR = "action.not.started.error";
        String INVALID_STATE_ERROR = "invalid.state.error";
        String OTP_EXPIRED_ERROR = "otp.expired.error";
        String LIMIT_REACHED_ERROR = "otp.no.more.new.codes.error";
        String BAD_CREDENTIALS_ERROR = "bad.credentials.error";
        String BLOCKED_ERROR = "blocked.error";
        String OTP_AUTH_REQUIRED = "otp.auth.required";
        String PASSWORDS_EQUAL_ERROR = "passwords.equal.error";
    }
}
