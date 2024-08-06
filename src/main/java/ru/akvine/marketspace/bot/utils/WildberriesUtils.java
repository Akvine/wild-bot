package ru.akvine.marketspace.bot.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class WildberriesUtils {
    public double calculateDiscountPrice(int price, int discount) {
        return price * (1 - ((double) discount / 100));
    }
}
