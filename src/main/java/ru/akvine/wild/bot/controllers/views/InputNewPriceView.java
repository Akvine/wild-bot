package ru.akvine.wild.bot.controllers.views;

import lombok.RequiredArgsConstructor;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;

@View
@RequiredArgsConstructor
public class InputNewPriceView implements BotView {
    private final BotKeyboardFactoryFacade facade;

    @Override
    public InlineKeyboard getKeyboard(String chatId, BotType botType) {
        return facade.resolve(botType, byState()).create(chatId);
    }

    @Override
    public String getMessage(String chatId) {
        return "Введите новую цену карточки: ";
    }

    @Override
    public ClientState byState() {
        return ClientState.INPUT_NEW_PRICE_MENU;
    }
}
