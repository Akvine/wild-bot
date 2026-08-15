package ru.akvine.wild.bot.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.constants.ApiErrorConstants;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.exceptions.ValidationException;

@Component
public class BotTypeValidator implements Validator<String> {
    @Override
    public void validate(String botType) {
        if (StringUtils.isBlank(botType)) {
            throw new ValidationException(
                    ApiErrorConstants.Validation.BOT_TYPE_BLANK_ERROR, "Bot type is blank. Field name: botType");
        }

        for (BotType value : BotType.values()) {
            if (value.getType().equals(botType)) {
                return;
            }
        }

        throw new ValidationException(
                ApiErrorConstants.Validation.BOT_TYPE_INVALID_ERROR, "Bot type is invalid. Field name: botType");
    }
}
