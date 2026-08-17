package ru.akvine.wild.bot.controllers.keyboard.max;

import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.max.MaxKeyboardFactory;

@Component
public class InputNewWildberriesTokenMaxKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
        return new InlineKeyboard(MaxKeyboardFactory.getBackKeyboard());
    }

    @Override
    public BotType getByType() {
        return BotType.MAX;
    }

    @Override
    public ClientState getByState() {
        return ClientState.INPUT_NEW_WILDBERRIES_TOKEN_MENU;
    }
}
