package ru.akvine.marketspace.bot.enums;

/**
 * Тип кампании с WB
 *  4 - кампания в каталоге
 *  5 - кампания в карточке товара
 *  6 - кампания в поиске
 *  7 - кампания в рекомендациях на главной странице
 *  8 - автоматическая кампания
 *  9 - поиск + каталог
 */
public enum AdvertType {
    CATALOG(4),
    CARD(5),
    SEARCH(6),
    RECOMMENDATIONS(7),
    AUTO(8),
    SEARCH_AND_CATALOG(9);

    private final int code;

    AdvertType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static AdvertType getByCode(int code) {
        for (AdvertType advertType : values()) {
            if (advertType.getCode() == code) {
                return advertType;
            }
        }
        throw new IllegalArgumentException("No type with code " + code + " found");
    }
}
