package ru.akvine.wild.bot.utils;

import java.util.UUID;
import lombok.experimental.UtilityClass;

/**
 * Генерация UUID, в том числе без дефисов и усечённых по длине.
 */
@UtilityClass
public class UUIDGenerator {
    private static final int START_INDEX = 0;

    /**
     * Генерирует новый случайный UUID в верхнем регистре.
     *
     * @return строковое представление UUID (с дефисами, в верхнем регистре)
     */
    public String uuid() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    /**
     * Генерирует новый случайный UUID и обрезает его до заданной длины.
     *
     * @param length требуемая длина результата в символах
     * @return первые {@code length} символов сгенерированного UUID
     */
    public String uuid(int length) {
        return uuid().substring(START_INDEX, length);
    }

    /**
     * Генерирует новый случайный UUID без дефисов.
     *
     * @return строковое представление UUID без дефисов, в верхнем регистре
     */
    public String uuidWithoutDashes() {
        return uuid().replaceAll("-", "");
    }
}
