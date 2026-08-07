package ru.akvine.wild.bot.controllers.keyboard.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.keyboard.BotKeyboardFactory;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.telegram.TelegramKeyboardFactory;

@Component
@RequiredArgsConstructor
public class InputNewPriceViewTelegramKeyboardFactory implements BotKeyboardFactory {
    @Override
    public InlineKeyboard create(String chatId) {
        return new InlineKeyboard(TelegramKeyboardFactory.getBackKeyboard());
    }

    @Override
    public BotType getByType() {
        return BotType.TELEGRAM;
    }

    @Override
    public ClientState getByState() {
        return ClientState.INPUT_NEW_PRICE_MENU;
    }
}
