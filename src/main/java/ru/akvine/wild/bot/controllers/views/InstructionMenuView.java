package ru.akvine.wild.bot.controllers.views;

import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;

@View
public class InstructionMenuView extends AbstractBotView {
    private static final String NEW_LINE = "\n";

    public InstructionMenuView(BotKeyboardFactoryFacade facade) {
        super(facade);
    }

    @Override
    public String getMessage(String chatId, BotType botType) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>Запуск рекламной кампании</b> \uD83D\uDE80: выберите")
                .append(NEW_LINE)
                .append("категорию товара, и бот автоматически")
                .append(NEW_LINE)
                .append("создаст новую рекламную кампанию или")
                .append(NEW_LINE)
                .append("запустит уже cозданную")
                .append(NEW_LINE)
                .append("<b>Управление ставками CPM</b> \uD83D\uDCB0: бот будет")
                .append(NEW_LINE)
                .append("автоматически регулировать ставки для")
                .append(NEW_LINE)
                .append("оптимального размещения рекламы.")
                .append(NEW_LINE)
                .append("<b>Настройка цен и скидок</b> \uD83C\uDFF7: введите")
                .append(NEW_LINE)
                .append("желаемую цену на товар, и бот рассчитает")
                .append(NEW_LINE)
                .append("необходимую скидку.")
                .append(NEW_LINE)
                .append("<b>Пополнение рекламного кабинета</b> \uD83D\uDCF2:")
                .append(NEW_LINE)
                .append("запросите QR-код для пополнения, и бот")
                .append(NEW_LINE)
                .append("выдаст его для оплаты.")
                .append(NEW_LINE)
                .append("<b>Оптимальное размещение рекламы</b> \uD83D\uDCC8:")
                .append(NEW_LINE)
                .append("бот обеспечит лучшее размещение ваших  объявлений");
        return sb.toString();
    }

    @Override
    public ClientState byState() {
        return ClientState.INSTRUCTIONS_MENU;
    }
}
