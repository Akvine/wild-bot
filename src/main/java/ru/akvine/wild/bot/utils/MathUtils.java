package ru.akvine.wild.bot.utils;

import lombok.experimental.UtilityClass;
import ru.akvine.wild.bot.exceptions.ValidationException;
import ru.akvine.wild.bot.constants.ApiErrorConstants;

@UtilityClass
public class MathUtils {
    private final static int MIN_VALUE = 0;

    public double round(double value, int roundAccuracy) {
        if (roundAccuracy < 0) {
            throw new ValidationException(
                    ApiErrorConstants.Validation.LESS_THEN_MIN_VALUE_ERROR,
                    "Round accuracy can't be less than min value = " + MIN_VALUE
            );
        }
        return Math.round(value * Math.pow(10, roundAccuracy)) / Math.pow(10, roundAccuracy);
    }
}
