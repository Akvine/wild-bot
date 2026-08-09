package ru.akvine.wild.bot.utils;

import com.google.common.base.Strings;
import java.security.SecureRandom;
import lombok.experimental.UtilityClass;

/**
 * Генерация одноразовых числовых кодов (OTP) для двухфакторной аутентификации.
 */
@UtilityClass
public class OneTimePasswordGenerator {
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Генерирует случайный числовой код заданной длины, дополняя его нулями слева при
     * необходимости (например, для {@code length = 4} код всегда состоит ровно из 4 цифр).
     *
     * @param length требуемая длина кода в цифрах
     * @return сгенерированный код в виде строки из {@code length} цифр
     */
    public String generate(int length) {
        return Strings.padStart(secureRandom.nextInt((int) Math.pow(10.0D, length) - 1) + "", length, '0');
    }
}
