package ru.akvine.wild.bot.unit.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.akvine.wild.bot.utils.WildberriesUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("Wildberries utils tests")
public class WildberriesUtilsTest {
    @Test
    @DisplayName("Price and discount can't be negative")
    public void price_and_discount_cant_be_negative() {
        int price = -1, discount = -1;
        assertThatThrownBy(() -> WildberriesUtils.calculateDiscountPrice(price, discount))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Price can't be negative")
    public void price_cant_be_negative() {
        int price = -1, discount = 50;
        String errorMessage = String.format("Price = (%s) can't be less than 0", price);
        assertThatThrownBy(() -> WildberriesUtils.calculateDiscountPrice(price, discount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(errorMessage);
    }

    @Test
    @DisplayName("Discount can't be negative")
    public void discount_cant_be_negative() {
        int price = 100, discount = -1;
        String errorMessage = String.format("Discount = (%s) can't be less than 0", discount);
        assertThatThrownBy(() -> WildberriesUtils.calculateDiscountPrice(price, discount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(errorMessage);
    }

    @Test
    @DisplayName("Discount can't be greater than 100")
    public void discount_cant_be_greater_than_one_hundred() {
        int price = 100, discount = 101;
        String errorMessage = String.format("Discount = (%s) can't be greater than 100", discount);
        assertThatThrownBy(() -> WildberriesUtils.calculateDiscountPrice(price, discount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(errorMessage);
    }

    @Test
    @DisplayName("Zero price and positive discount")
    public void zero_price_and_positive_discount() {
        int price = 0, discount = 20;
        int expected = 0;

        assertThat(WildberriesUtils.calculateDiscountPrice(price, discount)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Price and discount are positive")
    public void price_and_discount_are_positive() {
        int price = 100, discount = 50;
        int expected = 50;

        assertThat(WildberriesUtils.calculateDiscountPrice(price, discount)).isEqualTo(expected);
    }
}
