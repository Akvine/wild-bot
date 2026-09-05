package ru.akvine.wild.bot.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Операции с датами/временем: разница между датами в минутах, форматирование,
 * начало следующих суток, сравнение дат без учёта времени.
 */
@UtilityClass
public class DateUtils {
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Считает разницу между двумя датами в минутах.
     *
     * @param fromDate дата начала периода, не {@code null}
     * @param toDate   дата конца периода, не {@code null}
     * @return количество минут между {@code fromDate} и {@code toDate}
     * @throws IllegalArgumentException если {@code fromDate} или {@code toDate} равны {@code null}
     */
    public long getMinutes(@NotNull LocalDateTime fromDate, @NotNull LocalDateTime toDate) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("[from and to] dates can't be null");
        }
        return ChronoUnit.MINUTES.between(fromDate, toDate);
    }

    /**
     * Переводит количество лет в минуты, приблизительно учитывая високосные годы.
     *
     * @param years количество лет
     * @return приблизительное количество минут в {@code years} годах
     */
    public long getMinutes(int years) {
        int daysInYear = 365;
        int minutesInDay = 24 * 60;

        int leapYearsCount = years / 4;
        daysInYear += leapYearsCount;

        return (long) years * daysInYear * minutesInDay;
    }

    /**
     * Форматирует дату/время по формату по умолчанию ({@value #DEFAULT_DATE_TIME_FORMATTER}).
     *
     * @param localDateTime дата/время, может быть {@code null}
     * @return отформатированная строка либо {@code null}, если {@code localDateTime} равен {@code null}
     */
    @Nullable
    public String formatLocalDateTime(LocalDateTime localDateTime) {
        return formatLocalDateTime(localDateTime, DEFAULT_DATE_TIME_FORMATTER);
    }

    /**
     * Форматирует дату/время по заданному формату.
     *
     * @param localDateTime    дата/время, может быть {@code null}
     * @param dateTimeFormatter формат вывода
     * @return отформатированная строка либо {@code null}, если {@code localDateTime} равен {@code null}
     */
    @Nullable
    public String formatLocalDateTime(LocalDateTime localDateTime, DateTimeFormatter dateTimeFormatter) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.format(dateTimeFormatter);
    }

    /**
     * Преобразует {@link LocalDate} в {@link LocalDateTime}, устанавливая время на начало суток (полночь).
     * Если переданная дата равна {@code null}, метод возвращает {@code null}.
     *
     * @param localDate дата, которую требуется преобразовать; может быть {@code null}
     * @return объект {@link LocalDateTime}, соответствующий началу указанной даты,
     *         или {@code null}, если входной параметр равен {@code null}
     */
    @Nullable
    public LocalDateTime localDateToLocalDateTime(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }

        return LocalDateTime.of(localDate, LocalTime.MIDNIGHT);
    }

    /**
     * Возвращает начало следующих суток (00:00:00 завтрашнего дня) относительно текущей даты.
     *
     * @return дата/время начала следующих суток
     */
    public LocalDateTime getStartOfNextDay() {
        return LocalDate.now().plusDays(1).atStartOfDay();
    }

    /**
     * Проверяет, приходятся ли обе даты на один и тот же календарный день, игнорируя время.
     *
     * @param from первая дата/время
     * @param to   вторая дата/время
     * @return {@code true}, если обе даты не {@code null} и приходятся на один день
     */
    public boolean isSameDay(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return false;
        }
        return from.toLocalDate().isEqual(to.toLocalDate());
    }
}
