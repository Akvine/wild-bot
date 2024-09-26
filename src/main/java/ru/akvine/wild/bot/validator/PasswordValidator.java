package ru.akvine.wild.bot.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.constants.ApiErrorConstants;
import ru.akvine.wild.bot.exceptions.ValidationException;

import java.util.regex.Pattern;

@Component
public class PasswordValidator implements Validator<String> {
    private static final int DEFAULT_PWD_MIN_LENGTH = 8;
    private static final int DEFAULT_PWD_MAX_LENGTH = 20;

    private static final Pattern bigPattern = Pattern.compile("[A-Z]+");
    private static final Pattern smallPattern = Pattern.compile("[a-z]+");
    private static final Pattern digitsPattern = Pattern.compile("\\d+");
    private static final Pattern symbolsPattern = Pattern.compile("[!\"#\\$%&\\(\\)``\\*\\+,-\\/:;<=>\\?_]+");

    private static final String EMPTY_SPACE = " ";

    @Override
    public void validate(String password) {
        if (StringUtils.isBlank(password)) {
            throw new ValidationException(
                    ApiErrorConstants.Validation.REGISTRATION_PASSWORD_BLANK_ERROR,
                    "Password is blank. Field name: password");
        }

        if (password.length() < DEFAULT_PWD_MIN_LENGTH) {
            throw new ValidationException(
                    ApiErrorConstants.Validation.REGISTRATION_PASSWORD_INVALID_ERROR,
                    "Password is less than password min length = [" + DEFAULT_PWD_MIN_LENGTH + "]. Field name: password");
        }

        if (password.length() > DEFAULT_PWD_MAX_LENGTH) {
            throw new ValidationException(
                    ApiErrorConstants.Validation.REGISTRATION_PASSWORD_INVALID_ERROR,
                    "Password is greater than password max length = [" + DEFAULT_PWD_MAX_LENGTH + "]. Field name: password");
        }

        if (password.contains(EMPTY_SPACE)) {
            throw new ValidationException(
                    ApiErrorConstants.Validation.REGISTRATION_PASSWORD_INVALID_ERROR,
                    "Password contains empty spaces. Field name: password");
        }

        long findCount = 0;
        if (bigPattern.matcher(password).find()) {
            ++findCount;
        }

        if (smallPattern.matcher(password).find()) {
            ++findCount;
        }

        if (digitsPattern.matcher(password).find()) {
            ++findCount;
        }

        if (symbolsPattern.matcher(password).find()) {
            ++findCount;
        }

        if (findCount < 3) {
            throw new ValidationException(
                    ApiErrorConstants.Validation.REGISTRATION_PASSWORD_INVALID_ERROR,
                    "The password does not match the requirements. Field name: password"
            );
        }
    }
}
