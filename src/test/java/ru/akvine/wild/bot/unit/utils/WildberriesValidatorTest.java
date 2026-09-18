package ru.akvine.wild.bot.unit.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.akvine.wild.bot.controllers.validators.WildberriesValidator;
import ru.akvine.wild.bot.exceptions.InvalidDiscountException;
import ru.akvine.wild.bot.exceptions.InvalidPriceException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Wildberries validator tests")
public class WildberriesValidatorTest {
    private static final WildberriesValidator VALIDATOR = new WildberriesValidator();

    @Test
    @DisplayName("Price and discount can't be negative")
    public void price_and_discount_cant_be_negative() {
        int price = -1, discount = -1;
        assertThatThrownBy(() -> VALIDATOR.calculateDiscountPrice(price, discount))
                .isInstanceOf(InvalidDiscountException.class);
    }

    @Test
    @DisplayName("Price can't be negative")
    public void price_cant_be_negative() {
        int price = -1, discount = 50;
        String errorMessage = String.format("Price = (%s) can't be less or equals 0", price);
        assertThatThrownBy(() -> VALIDATOR.calculateDiscountPrice(price, discount))
                .isInstanceOf(InvalidPriceException.class)
                .hasMessage(errorMessage);
    }

    @Test
    @DisplayName("Discount can't be negative")
    public void discount_cant_be_negative() {
        int price = 100, discount = -1;
        String errorMessage = String.format("Discount = (%s) can't be less than 0", discount);
        assertThatThrownBy(() -> VALIDATOR.calculateDiscountPrice(price, discount))
                .isInstanceOf(InvalidDiscountException.class)
                .hasMessage(errorMessage);
    }

    @Test
    @DisplayName("Discount can't be greater than 100")
    public void discount_cant_be_greater_than_one_hundred() {
        int price = 100, discount = 101;
        String errorMessage = String.format("Discount = (%s) can't be greater than 100", discount);
        assertThatThrownBy(() -> VALIDATOR.calculateDiscountPrice(price, discount))
                .isInstanceOf(InvalidDiscountException.class)
                .hasMessage(errorMessage);
    }

    @Test
    @DisplayName("Zero price and positive discount")
    public void zero_price_and_positive_discount() {
        int price = 0, discount = 20;
        String errorMessage = String.format("Price = (%s) can't be less or equals 0", price);
        assertThatThrownBy(() -> VALIDATOR.calculateDiscountPrice(price, discount))
                .isInstanceOf(InvalidPriceException.class)
                .hasMessage(errorMessage);
    }

    @Test
    @DisplayName("Price and discount are positive")
    public void price_and_discount_are_positive() {
        int price = 100, discount = 50;
        int expected = 50;

        assertThat(VALIDATOR.calculateDiscountPrice(price, discount)).isEqualTo(expected);
    }
}
