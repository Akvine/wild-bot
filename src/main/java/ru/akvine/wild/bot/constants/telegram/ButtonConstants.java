package ru.akvine.wild.bot.constants.telegram;

public final class ButtonConstants {

    private ButtonConstants() throws IllegalAccessException {
        throw new IllegalAccessException(
                "Calling " + ButtonConstants.class.getSimpleName() + " constructor is prohibited!");
    }

    public static final String BACK_BUTTON_TEXT = "⬅Назад";
    public static final String TESTS_MENU = "Мои тесты \uD83D\uDE80";
    public static final String INSTRUCTIONS_FOR_USE_BUTTON_TEXT = "Инструкция по использованию❓";

    public static final String ADD_SUBSCRIPTION_BUTTON_TEXT = "Оформить подписку \uD83D\uDD14";
    public static final String PAY_SUBSCRIPTION_BUTTON_TEXT = "Оплатить";

    public static final String START_TEST_BUTTON_TEXT = "Запуск теста \uD83D\uDE80";
    public static final String LIST_STARTED_TESTS_BUTTON_TEXT = "Список запущенных тестов \uD83D\uDCDD";
    public static final String FILL_ADVERTISING_ACCOUNT_BUTTON_TEXT = "Пополнить рекламный кабинет \uD83D\uDCF2";
    public static final String WILDBERRIES_ACCOUNT_SETTINGS_BUTTON_TEXT = "Настройки аккаунта";

    public static final String REVOKE_TOKEN_BUTTON_TEXT = "Отозвать токен Wildberries";
    public static final String CHANGE_WAREHOUSE_BUTTON_TEXT = "Сменить ID склада";

    public static final String DETAIL_TEST_INFORMATION_BUTTON_TEXT = "Детальная информация по тестам \uD83D\uDCCA";

    public static final String GENERATE_REPORT_BUTTON_TEXT = "Сгенерировать отчет по тестам ✍";
    public static final String START_GENERATION_BUTTON_TEXT = "Запросить отчет \uD83D\uDCC4";

    public static final String QUERY_QR_CODE_BUTTON_TEXT = "Запросить QR \uD83D\uDCF2";

    public static final String MALE_BUTTON_TEXT = "Мужской";
    public static final String FEMALE_BUTTON_TEXT = "Женский";

    public static final String CHOOSE_CATEGORY_TEXT = "Выберите категорию: ";

    public static final String CHANGE_PRICE_BUTTON_TEXT = "Поменять";
    public static final String KEEP_PRICE_BUTTON_TEXT = "Оставить";
}
