package ru.akvine.wild.bot.controllers.views;

import lombok.RequiredArgsConstructor;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;

@RequiredArgsConstructor
public abstract class AbstractBotView implements BotView {
    private final BotKeyboardFactoryFacade facade;

    @Override
    public InlineKeyboard getKeyboard(String chatId, BotType botType) {
        return facade.resolve(botType, byState()).create(chatId);
    }
}
