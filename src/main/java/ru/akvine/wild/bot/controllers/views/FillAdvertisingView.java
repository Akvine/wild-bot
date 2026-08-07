package ru.akvine.wild.bot.controllers.views;

import lombok.RequiredArgsConstructor;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;

@View
@RequiredArgsConstructor
public class FillAdvertisingView implements BotView {
    private final static String NEW_LINE = "\n";

    private final BotKeyboardFactoryFacade facade;

    @Override
    public InlineKeyboard getKeyboard(String chatId, BotType botType) {
        return facade.resolve(botType, byState()).create(chatId);
    }

    @Override
    public String getMessage(String chatId) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("Пополнить рекламный кабинет \uD83D\uDCF2:").append(NEW_LINE)
                .append("Запросите QR-код для пополнения").append(NEW_LINE)
                .append("рекламного кабинета, и бот выдаст его для  оплаты, пополнит счётчик тестов.").append(NEW_LINE)
                .append("Стоимость одной попытки на тест: 500 руб.");
        return sb.toString();
    }

    @Override
    public ClientState byState() {
        return ClientState.FILL_ADVERTISING_ACCOUNT_MENU;
    }
}
