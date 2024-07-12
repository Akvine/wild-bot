package ru.akvine.marketspace.bot.infrastructure.impl;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ClientSessionData {
    private String choosenCategoryId;
    private byte[] uploadedCardPhoto;
    private boolean inputNewCardPriceAndDiscount;
    private int newCardPrice;
    private int newCardDiscount;
    private String lockedAdvertId;
}
