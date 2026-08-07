package ru.akvine.wild.bot.controllers.views;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;
import ru.akvine.wild.bot.telegram.TelegramKeyboardFactory;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.PAY_SUBSCRIPTION_BUTTON_TEXT;

@View
@RequiredArgsConstructor
public class SubscriptionMenuView implements BotView {
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
                .append("Оформить подписку в два клика.").append(NEW_LINE)
                .append("Стоимость месячной подписки составляет 4900 рублей");
        return sb.toString();
    }

    @Override
    public ClientState byState() {
        return ClientState.SUBSCRIBE_MENU;
    }
}
