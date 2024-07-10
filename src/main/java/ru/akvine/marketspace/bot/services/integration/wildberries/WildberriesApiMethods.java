package ru.akvine.marketspace.bot.services.integration.wildberries;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WildberriesApiMethods {
    GET_CARD_LIST("https://suppliers-api.wildberries.ru","/content/v2/get/cards/list?locale=ru"),
    GET_ADVERTS("https://advert-api.wb.ru", "/adv/v1/promotion/count"),
    ADVERT_BUDGET_DEPOSIT("https://advert-api.wb.ru","/adv/v1/budget/deposit"),
    START_ADVERT("https://advert-api.wb.ru", "/adv/v0/start"),
    PAUSE_ADVERT("https://advert-api.wb.ru","/adv/v0/pause"),
    CHANGE_ADVERT_CPM("https://advert-api.wb.ru", "/adv/v0/cpm"),
    RENAME_ADVERT("https://advert-api.wb.ru", "/adv/v0/rename"),
    GET_ADVERTS_INFO( "https://advert-api.wb.ru", "/adv/v1/promotion/adverts"),
    GET_ADVERT_STATISTIC("https://advert-api.wb.ru", "/adv/v1/auto/stat" ),
    UPLOAD_CARD_PHOTO("https://suppliers-api.wildberries.ru","/content/v3/media/file"),
    ADVERT_BUDGET_INFO("https://advert-api.wb.ru","/adv/v1/budget"),
    CHANGE_CARD_STOCKS("https://suppliers-api.wildberries.ru", "/api/v3/stocks/"),
    GET_ADVERTS_FULL_STATISTIC("https://advert-api.wb.ru", "/adv/v2/fullstats");

    private final String url;
    private final String method;
}
