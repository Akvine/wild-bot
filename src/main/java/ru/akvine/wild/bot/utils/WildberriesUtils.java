package ru.akvine.wild.bot.utils;

import lombok.experimental.UtilityClass;

/**
 * Расчёт цены с учётом скидки, эмулирующий формулу Wildberries.
 */
@UtilityClass
public class WildberriesUtils {
    /**
     * Считает цену товара с учётом скидки.
     *
     * @param price    цена без скидки, не отрицательная
     * @param discount скидка в процентах, от 0 до 100
     * @return цена после применения скидки
     * @throws IllegalArgumentException если {@code price} отрицательная либо {@code discount}
     *                                  вне диапазона [0, 100]
     */
    public double calculateDiscountPrice(int price, int discount) {
        validateArguments(price, discount);
        return price * (1 - ((double) discount / 100));
    }

    /**
     * Проверяет корректность цены и скидки.
     *
     * @param price    цена без скидки
     * @param discount скидка в процентах
     * @throws IllegalArgumentException если {@code price} отрицательная либо {@code discount}
     *                                  вне диапазона [0, 100]
     */
    private void validateArguments(int price, int discount) {
        if (price < 0 && discount < 0) {
            String errorMessage =
                    String.format("Price = (%s) and discount = (%s) can't be less than 0", price, discount);
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
