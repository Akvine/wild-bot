package ru.akvine.wild.bot.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.constants.ApiErrorConstants;
import ru.akvine.wild.bot.exceptions.ValidationException;

import java.util.regex.Pattern;

@Component
public class EmailValidator implements Validator<String> {
    private static final String EMAIL_PATTERN =
            "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

    @Override
    public void validate(String email) {
        if (StringUtils.isBlank(email)) {
            throw new ValidationException(ApiErrorConstants.Validation.EMAIL_BLANK_ERROR,
                    "Email is blank. Field name: email");
        }
        if (!Pattern.matches(EMAIL_PATTERN, email)) {
            throw new ValidationException(ApiErrorConstants.Validation.EMAIL_INVALID_ERROR,
                    "Email is invalid. Field name: email");
        }
    }
}
