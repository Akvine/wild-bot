package ru.akvine.wild.bot.controllers.views;

import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.*;

@View
public class TestsMainView extends AbstractBotView {
    private final static String NEW_LINE = "\n";

    public TestsMainView(BotKeyboardFactoryFacade facade) {
        super(facade);
    }

    @Override
    public String getMessage(String chatId) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("<b>").append(START_TEST_BUTTON_TEXT).append("</b>").append(NEW_LINE)
                .append("Выберите категорию товара, и бот автоматически создаст").append(NEW_LINE)
                .append("новую рекламную кампанию или запустит уже").append(NEW_LINE)
                .append("созданную").append(NEW_LINE)
                .append("<b>").append(LIST_STARTED_TESTS_BUTTON_TEXT).append("</b>").append(NEW_LINE)
                .append("Запросите вывод списка запущенных тестов").append(NEW_LINE)
                .append("<b>").append(FILL_ADVERTISING_ACCOUNT_BUTTON_TEXT).append("</b>").append(NEW_LINE)
                .append("Запросите QR-код для пополнения").append(NEW_LINE)
                .append("рекламного кабинета, и бот выдаст его для").append(NEW_LINE)
                .append("оплаты, пополнит счётчик тестов. ").append(NEW_LINE)
                .append("<b>").append(GENERATE_REPORT_BUTTON_TEXT).append("</b>").append(": ").append(NEW_LINE)
                .append("Бот сгенерирует отчёт в формате Excel").append(NEW_LINE)
                .append("по всем проведенным тестам").append(NEW_LINE)
                .append("<b>").append(DETAIL_TEST_INFORMATION_BUTTON_TEXT).append("</b>").append(NEW_LINE)
                .append("Вы можете через id теста посмотреть детальную информацию");
        return sb.toString();
    }

    @Override
    public ClientState byState() {
        return ClientState.TESTS_MENU;
    }
}
