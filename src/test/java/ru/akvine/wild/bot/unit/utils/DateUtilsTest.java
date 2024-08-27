package ru.akvine.wild.bot.unit.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.akvine.wild.bot.utils.DateUtils;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("Date utils tests")
public class DateUtilsTest {
    @Test
    @DisplayName("Get minutes - FROM and TO dates can't be null")
    public void get_minutes_from_and_to_dates_cant_be_null() {
        assertThatThrownBy(() -> DateUtils.getMinutes(null, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Get minutes from dates range")
    public void get_minutes_from_dates_range() {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusMinutes(10);
        int expected = 10;

        assertThat(DateUtils.getMinutes(startDate, endDate)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Get minutes from years without leaps")
    public void get_minutes_from_year_without_leaps() {
        int expected = 525600;
        assertThat(DateUtils.getMinutes(1)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Get minutes from years with one leap year")
    public void get_minutes_from_years_with_one_leap_year() {
        int expected = 2635200;
        assertThat(DateUtils.getMinutes(5)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Expected null while passing null in format local date time to string")
    public void expected_null_while_passing_null_in_format_local_date_time_to_string() {
        String expected = null;
        assertThat(DateUtils.formatLocalDateTime(null)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Convert local date time to string")
    public void convert_local_date_time_to_string() {
        String expected = "2024-08-18 14:00:00";
        LocalDateTime argument = LocalDateTime.of(2024, Month.AUGUST, 18, 14, 0, 0);
        assertThat(DateUtils.formatLocalDateTime(argument)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Check is same day - return false if any argument is null")
    public void check_is_same_day_return_false_if_any_argument_is_null() {
        boolean expected = false;
        assertThat(DateUtils.isSameDay(null, LocalDateTime.now())).isEqualTo(expected);
    }

    @Test
    @DisplayName("Check is same day by date range")
    public void check_is_same_day_by_date_range() {
        boolean expected = true;
        LocalDateTime startRange = LocalDateTime.now();
        LocalDateTime endRange = startRange.plusSeconds(1);

        assertThat(DateUtils.isSameDay(endRange, startRange)).isEqualTo(expected);
    }
}
