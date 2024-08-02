package ru.akvine.marketspace.bot.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@UtilityClass
public class DateUtils {
    public final static DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public long getMinutes(LocalDateTime fromDate, LocalDateTime toDate) {
        return ChronoUnit.MINUTES.between(fromDate, toDate);
    }

    public long getMinutes(int years) {
        int daysInYear = 365;
        int minutesInDay = 24 * 60;

        return (long) years * daysInYear * minutesInDay;
    }

    @Nullable
    public LocalDateTime dateToLocalDate(Date date) {
        if (date == null) {
            return null;
        }

        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
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
}
