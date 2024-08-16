package ru.akvine.marketspace.bot.resolvers.controllers.views;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.telegram.KeyboardFactory;

@Component
public class FinishGenerationReportView implements TelegramView {
    @Override
    public InlineKeyboardMarkup getKeyboard(String chatId) {
        return KeyboardFactory.getBackKeyboard();
    }

    @Override
    public String getMessage(String chatId) {
        return "Ваш отчёт готов, вы можете перейти по команде  назад для запуска ещё одного теста";
    }

    @Override
    public ClientState byState() {
        return ClientState.FINISH_GENERATION_REPORT_MENU;
    }
}
