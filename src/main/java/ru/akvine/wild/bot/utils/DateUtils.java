package ru.akvine.marketspace.bot.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@UtilityClass
public class DateUtils {
    public final static DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public long getMinutes(@NotNull LocalDateTime fromDate, @NotNull LocalDateTime toDate) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("[from and to] dates can't be null");
        }
        return ChronoUnit.MINUTES.between(fromDate, toDate);
    }

    public long getMinutes(int years) {
        int daysInYear = 365;
        int minutesInDay = 24 * 60;

        int leapYearsCount = years / 4;
        daysInYear += leapYearsCount;

        return (long) years * daysInYear * minutesInDay;
    }

    @Nullable
    public String formatLocalDateTime(LocalDateTime localDateTime) {
        return formatLocalDateTime(localDateTime, DEFAULT_DATE_TIME_FORMATTER);
    }

    @Nullable
    public String formatLocalDateTime(LocalDateTime localDateTime, DateTimeFormatter dateTimeFormatter) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.format(dateTimeFormatter);
    }

    public LocalDateTime getStartOfNextDay() {
        return LocalDate.now().plusDays(1).atStartOfDay();
    }

    public boolean isSameDay(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return false;
        }
        return from.toLocalDate().isEqual(to.toLocalDate());
    }
}
