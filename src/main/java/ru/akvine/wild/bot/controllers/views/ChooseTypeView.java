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

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.FEMALE_BUTTON_TEXT;
import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.MALE_BUTTON_TEXT;

@View
@RequiredArgsConstructor
public class ChooseTypeView implements BotView {
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
                .append("Запуск рекламной кампании \uD83D\uDE80:").append(NEW_LINE)
                .append("Выберите в какой категории будет тестироваться товар:");
        return sb.toString();
    }

    @Override
    public ClientState byState() {
        return ClientState.CHOOSE_TYPE_MENU;
    }
}
