package ru.akvine.wild.bot.controllers.views;

import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;

@View
public class FillAdvertisingView extends AbstractBotView {
    private static final String NEW_LINE = "\n";

    public FillAdvertisingView(BotKeyboardFactoryFacade facade) {
        super(facade);
    }

    @Override
    public String getMessage(String chatId, BotType botType) {
        StringBuilder sb = new StringBuilder();
        sb.append("Пополнить рекламный кабинет \uD83D\uDCF2:")
                .append(NEW_LINE)
                .append("Запросите QR-код для пополнения")
                .append(NEW_LINE)
                .append("рекламного кабинета, и бот выдаст его для  оплаты, пополнит счётчик тестов.")
                .append(NEW_LINE)
                .append("Стоимость одной попытки на тест: 500 руб.");
        return sb.toString();
    }

    @Override
    public ClientState byState() {
        return ClientState.FILL_ADVERTISING_ACCOUNT_MENU;
    }
}
