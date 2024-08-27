package ru.akvine.wild.bot.unit.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.akvine.wild.bot.exceptions.ValidationException;
import ru.akvine.wild.bot.utils.MathUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("Math utils tests")
public class MathUtilsTest {
    @Test
    @DisplayName("Round value with accuracy is 2")
    public void round_value_with_accuracy_is_two() {
        double value = 10.485;
        double expected = 10.49;

        double result = MathUtils.round(value, 2);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Round value can't be less than min value - zero")
    public void round_value_cant_be_less_than_min_value() {
        double value = 10.485;

        assertThatThrownBy(() -> MathUtils.round(value, -15))
                .isInstanceOf(ValidationException.class);
    }
}
