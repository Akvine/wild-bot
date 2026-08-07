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

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.*;

@View
@RequiredArgsConstructor
public class MainMenuView implements BotView {
    private final static String NEW_LINE = "\n";
    private final static String DOUBLE_TABULATION = "\t\t";

    private final BotKeyboardFactoryFacade facade;

    @Override
    public InlineKeyboard getKeyboard(String chatId, BotType botType) {
        return facade.resolve(botType, byState()).create(chatId);
    }

    @Override
    public String getMessage(String chatId) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("Привет! Я бот для автоматических A/B  тестирований").append(NEW_LINE)
                .append("контента на Wildberries \uD83D\uDCCA.").append(NEW_LINE)
                .append("С моей помощью вы сможете легко и быстро  тестировать контент карточки товара.").append(NEW_LINE)
                .append("\uD83D\uDD0D Вот что я могу для вас сделать: ").append(NEW_LINE)
                .append(DOUBLE_TABULATION).append("1. Запускать рекламные кампании в  специальном кабинете \uD83D\uDE80").append(NEW_LINE)
                .append(DOUBLE_TABULATION).append("2. Управлять ставками CPM \uD83D\uDCB0").append(NEW_LINE)
                .append(DOUBLE_TABULATION).append("3. Настраивать цены и скидки на товары  \uD83C\uDFF7").append(NEW_LINE)
                .append(DOUBLE_TABULATION).append("4. Выдавать QR-коды для пополнения  рекламного кабинета \uD83D\uDCF2").append(NEW_LINE)
                .append(DOUBLE_TABULATION).append("5. Обеспечивать оптимальное  размещение рекламы и снятие  метрик \uD83D\uDCC8").append(NEW_LINE)
                .append("Я помогу вам заменить ручные тесты").append(NEW_LINE)
                .append("автоматическими \uD83E\uDD16, чтобы вы могли").append(NEW_LINE)
                .append("сосредоточиться на более важных задачах.").append(NEW_LINE)
                .append("Для начала работы просто выберите нужную").append(NEW_LINE)
                .append("команду из меню. Если у вас возникнут").append(NEW_LINE)
                .append("вопросы, не стесняйтесь обращаться за").append(NEW_LINE)
                .append("помощью через команду /help").append(NEW_LINE)
                .append("Давайте  начнем!");
        return sb.toString();
    }

    @Override
    public ClientState byState() {
        return ClientState.MAIN_MENU;
    }
}
