package ru.akvine.wild.bot.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.enums.AdvertStatus;
import ru.akvine.wild.bot.exceptions.ValidationException;
import ru.akvine.wild.bot.constants.ApiErrorConstants;

@Component
public class AdvertStatusValidator implements Validator<String> {
    @Override
    public void validate(String status) {
        if (StringUtils.isBlank(status)) {
            throw new ValidationException(
                    ApiErrorConstants.Validation.ADVERT_STATUS_BLANK_ERROR,
                    "Advert status is blank"
            );
        }

        try {
            AdvertStatus.valueOf(status.toUpperCase());
        } catch (Exception exception) {
            throw new ValidationException(
                    ApiErrorConstants.Validation.ADVERT_STATUS_INVALID_ERROR,
                    "Advert status is invalid"
            );
        }
    }
}
