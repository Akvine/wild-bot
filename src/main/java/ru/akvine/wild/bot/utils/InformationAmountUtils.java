package ru.akvine.wild.bot.utils;

import lombok.experimental.UtilityClass;

/**
 * Конвертация объёма информации между мегабайтами и байтами.
 */
@UtilityClass
public class InformationAmountUtils {
    /**
     * Переводит мегабайты в байты.
     *
     * @param megabytesCount количество мегабайт, не отрицательное
     * @return эквивалент в байтах
     * @throws IllegalArgumentException если {@code megabytesCount} отрицательное
     */
    public int fromMegabytesToBytes(int megabytesCount) {
        validate(megabytesCount);
        return (int) (megabytesCount * Math.pow(10, 6));
    }

    /**
     * Переводит байты в мегабайты.
     *
     * @param bytes количество байт, не отрицательное
     * @return эквивалент в мегабайтах
     * @throws IllegalArgumentException если {@code bytes} отрицательное
     */
    public double fromBytesToMegabytes(int bytes) {
        validate(bytes);
        return bytes / Math.pow(10, 6);
    }

    /**
     * Проверяет, что переданное значение не отрицательное.
     *
     * @param argument проверяемое значение
     * @throws IllegalArgumentException если {@code argument} отрицательное
     */
    private void validate(int argument) {
        if (argument < 0) {
            throw new IllegalArgumentException("Argument is negative!");
        }
    }
}
