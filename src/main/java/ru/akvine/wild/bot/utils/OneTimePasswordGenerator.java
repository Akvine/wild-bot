package ru.akvine.wild.bot.utils;

import com.google.common.base.Strings;
import java.security.SecureRandom;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OneTimePasswordGenerator {
    private static final SecureRandom secureRandom = new SecureRandom();

    public String generate(int length) {
        return Strings.padStart(secureRandom.nextInt((int) Math.pow(10.0D, length) - 1) + "", length, '0');
    }
}
