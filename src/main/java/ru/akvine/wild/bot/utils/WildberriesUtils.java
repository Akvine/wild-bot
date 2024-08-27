package ru.akvine.wild.bot.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class WildberriesUtils {
    public double calculateDiscountPrice(int price, int discount) {
        validateArguments(price, discount);
        return price * (1 - ((double) discount / 100));
    }

    private void validateArguments(int price, int discount) {
        if (price < 0 && discount < 0) {
            String errorMessage = String.format(
                    "Price = (%s) and discount = (%s) can't be less than 0",
                    price, discount);
            throw new IllegalArgumentException(errorMessage);
        }
        if (price < 0) {
            String errorMessage = String.format("Price = (%s) can't be less than 0", price);
            throw new IllegalArgumentException(errorMessage);
        }
        if (discount < 0) {
            String errorMessage = String.format("Discount = (%s) can't be less than 0", discount);
            throw new IllegalArgumentException(errorMessage);
        }
        if (discount > 100) {
            String errorMessage = String.format("Discount = (%s) can't be greater than 100", discount);
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
