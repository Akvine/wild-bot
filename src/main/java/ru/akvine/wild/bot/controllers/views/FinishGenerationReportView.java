package ru.akvine.wild.bot.controllers.views;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;
import ru.akvine.wild.bot.telegram.TelegramKeyboardFactory;

@View
@RequiredArgsConstructor
public class FinishGenerationReportView implements BotView {
    private final BotKeyboardFactoryFacade facade;

    @Override
    public InlineKeyboard getKeyboard(String chatId, BotType botType) {
        return facade.resolve(botType, byState()).create(chatId);
    }

    @Override
    public String getMessage(String chatId) {
        return "Ваш отчёт готов, вы можете перейти по команде назад для запуска ещё одного теста";
    }

    @Override
    public ClientState byState() {
        return ClientState.FINISH_GENERATION_REPORT_MENU;
    }
}
