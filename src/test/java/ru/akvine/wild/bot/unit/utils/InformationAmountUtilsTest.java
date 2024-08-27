package ru.akvine.wild.bot.unit.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.akvine.wild.bot.utils.InformationAmountUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("Information utils tests")
public class InformationAmountUtilsTest {

    @Test
    @DisplayName("Convert from megabytes to bytes with negative argument")
    public void convert_from_megabytes_to_bytes_with_negative_argument() {
        int argument = -1;
        assertThatThrownBy(() -> InformationAmountUtils.fromBytesToMegabytes(argument))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Convert from megabytes to bytes with non-negative argument")
    public void convert_from_megabytes_to_bytes_with_zero_argument() {
        int argument = 5;
        int expected = 5000000;

        assertThat(InformationAmountUtils.fromMegabytesToBytes(argument)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Convert from bytes to megabytes with negative argument")
    public void convert_from_bytes_to_megabytes_with_negative_argument() {
        int argument = -1;
        assertThatThrownBy(() -> InformationAmountUtils.fromMegabytesToBytes(argument))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Convert from bytes to megabytes with non-negative argument")
    public void convert_from_bytes_to_megabytes_with_non_negative_argument() {
        int argument = 1000;
        double expected = 0.001;

        assertThat(InformationAmountUtils.fromBytesToMegabytes(argument)).isEqualTo(expected);
    }

}
