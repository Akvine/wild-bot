package ru.akvine.wild.bot.controllers.views;

import lombok.RequiredArgsConstructor;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.CHOOSE_CATEGORY_TEXT;

@View
@RequiredArgsConstructor
public class ChooseCategoryView implements BotView {
    private final BotKeyboardFactoryFacade facades;

    @Override
    public InlineKeyboard getKeyboard(String chatId, BotType botType) {
        return facades.resolve(botType, byState()).create(chatId);
    }

    @Override
    public String getMessage(String chatId) {
        return CHOOSE_CATEGORY_TEXT;
    }

    @Override
    public ClientState byState() {
        return ClientState.CHOOSE_CATEGORY_MENU;
    }
}
