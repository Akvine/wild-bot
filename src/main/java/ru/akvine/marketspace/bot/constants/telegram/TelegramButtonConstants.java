package ru.akvine.marketspace.bot.constants.telegram;

public final class TelegramButtonConstants {

    private TelegramButtonConstants() throws IllegalAccessException {
        throw new IllegalAccessException("Calling " + TelegramButtonConstants.class.getSimpleName() + " constructor is prohibited!");
    }

    public final static String BACK_BUTTON_TEXT = "⬅Назад";
    public final static String TESTS_MENU = "Мои тесты \uD83D\uDE80";
    public final static String INSTRUCTIONS_FOR_USE_BUTTON_TEXT = "Инструкция по использованию❓";

    public final static String ADD_SUBSCRIPTION_BUTTON_TEXT = "Оформить подписку \uD83D\uDD14";
    public final static String PAY_SUBSCRIPTION_BUTTON_TEXT = "Оплатить";

    public final static String START_TEST_BUTTON_TEXT = "Запуск теста \uD83D\uDE80";
    public final static String LIST_STARTED_TESTS_BUTTON_TEXT = "Список запущенных тестов \uD83D\uDCDD";
    public final static String FILL_ADVERTISING_ACCOUNT_BUTTON_TEXT = "Пополнить рекламный кабинет \uD83D\uDCF2";

    public final static String DETAIL_TEST_INFORMATION_BUTTON_TEXT = "Детальная информация по тестам \uD83D\uDCCA";

    public final static String GENERATE_REPORT_BUTTON_TEXT = "Сгенерировать отчет по тестам ✍";
    public final static String START_GENERATION_BUTTON_TEXT = "Запросить отчет \uD83D\uDCC4";

    public final static String QUERY_QR_CODE_BUTTON_TEXT = "Запросить QR \uD83D\uDCF2";

    public final static String MALE_BUTTON_TEXT = "Мужской \uD83D\uDC68";
    public final static String FEMALE_BUTTON_TEXT = "Женский \uD83D\uDC69";

    public final static String CHOOSE_CATEGORY_TEXT = "Выберите категорию: ";

    public final static String CHANGE_PRICE_BUTTON_TEXT = "Поменять";
    public final static String KEEP_PRICE_BUTTON_TEXT = "Оставить";
}
