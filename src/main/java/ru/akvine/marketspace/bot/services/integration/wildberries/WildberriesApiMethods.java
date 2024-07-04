package ru.akvine.marketspace.bot.services.integration.wildberries;

public enum WildberriesApiMethods {
    GET_CARD_LIST("/content/v2/get/cards/list?locale=ru", "https://suppliers-api.wildberries.ru"),
    GET_ADVERTS("/adv/v1/promotion/count", "https://advert-api.wb.ru"),
    ADVERT_BUDGET_DEPOSIT("/adv/v1/budget/deposit", "https://advert-api.wb.ru"),
    START_ADVERT("/adv/v0/start", "https://advert-api.wb.ru"),
    PAUSE_ADVERT("/adv/v0/pause", "https://advert-api.wb.ru"),
    CHANGE_ADVERT_CPM("/adv/v0/cpm", "https://advert-api.wb.ru"),
    RENAME_ADVERT("/adv/v0/rename", "https://advert-api.wb.ru"),
    GET_ADVERTS_INFO("/adv/v1/promotion/adverts", "https://advert-api.wb.ru"),
    GET_ADVERT_STATISTIC("/adv/v1/auto/stat", "https://advert-api.wb.ru"),
    UPLOAD_CARD_PHOTO("/content/v3/media/file", "https://suppliers-api.wildberries.ru"),
    ADVERT_BUDGET_INFO("/adv/v1/budget", "https://advert-api.wb.ru"),
    CHANGE_CARD_STOCKS("/api/v3/stocks/", "https://suppliers-api.wildberries.ru"),
    GET_ADVERTS_FULL_STATISTIC("/adv/v2/fullstats", "https://advert-api.wb.ru");

    private final String method;
    private final String url;

    WildberriesApiMethods(String method, String url) {
        this.method = method;
        this.url = url;
    }

    public String getMethod() {
        return this.method;
    }

    public String getUrl() {
        return this.url;
    }
}
