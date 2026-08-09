package ru.akvine.wild.bot.utils;

import lombok.experimental.UtilityClass;
import ru.akvine.wild.bot.constants.ApiErrorConstants;
import ru.akvine.wild.bot.exceptions.ValidationException;

/**
 * Округление чисел с заданной точностью.
 */
@UtilityClass
public class MathUtils {
    private static final int MIN_VALUE = 0;

    /**
     * Округляет число до заданного количества знаков после запятой.
     *
     * @param value         округляемое значение
     * @param roundAccuracy количество знаков после запятой, не отрицательное
     * @return {@code value}, округлённое до {@code roundAccuracy} знаков
     * @throws ValidationException если {@code roundAccuracy} отрицательное
     */
    public double round(double value, int roundAccuracy) {
        if (roundAccuracy < 0) {
            throw new ValidationException(
                    ApiErrorConstants.Validation.LESS_THEN_MIN_VALUE_ERROR,
                    "Round accuracy can't be less than min value = " + MIN_VALUE);
        }
        return Math.round(value * Math.pow(10, roundAccuracy)) / Math.pow(10, roundAccuracy);
    }
}
