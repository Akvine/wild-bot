package ru.akvine.marketspace.bot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DebitType {
    ACCOUNT(0),
    BALANCE(1),
    BONUSES(3);

    private final int code;
}
