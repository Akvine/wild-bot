package ru.akvine.marketspace.bot.services;

import org.springframework.stereotype.Service;

@Service
public class WildberriesCalculationService {
    public double calculateDiscountPrice(int price, int discount) {
        return price * (1 - ((double) discount / 100));
    }
}
