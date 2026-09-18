package ru.akvine.wild.bot.controllers.validators;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.exceptions.InvalidDiscountException;
import ru.akvine.wild.bot.exceptions.InvalidPriceException;

/**
 * Расчёт цены с учётом скидки, эмулирующий формулу Wildberries.
 */
@Component
public class WildberriesValidator {
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
        validateDiscount(discount);
        validatePrice(price);
        return price * (1 - ((double) discount / 100));
    }

    /**
     * Проверяет цену товара
     *
     * @param price цена товара
     * @throws InvalidPriceException если {@code price} отрицательная или равна 0
     */
    public void validatePrice(int price) {
        if (price <= 0) {
            String errorMessage = String.format("Price = (%s) can't be less or equals 0", price);
            throw new InvalidPriceException(errorMessage);
        }
    }

    /**
     * Проверяет скидку товара
     *
     * @param discount скидка в процентах, от 0 до 100
     * @throws InvalidDiscountException если {@code discount}
     *                                  вне диапазона [0, 100]
     */
    public void validateDiscount(int discount) {
        if (discount < 0) {
            String errorMessage = String.format("Discount = (%s) can't be less than 0", discount);
            throw new InvalidDiscountException(errorMessage);
        }
        if (discount > 100) {
            String errorMessage = String.format("Discount = (%s) can't be greater than 100", discount);
            throw new InvalidDiscountException(errorMessage);
        }
    }
}
