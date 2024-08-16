package ru.akvine.marketspace.bot.resolvers.controllers.view;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.telegram.KeyboardFactory;

@Component
public class InstructionMenuView implements TelegramView {
    @Override
    public InlineKeyboardMarkup getKeyboard(String chatId) {
        return KeyboardFactory.getBackKeyboard();
    }

    @Override
    public String getMessage(String chatId) {
        return "Запуск рекламной кампании \uD83D\uDE80:выберите  категорию товара, и бот автоматически  создаст новую рекламную кампанию или  запустит уже созданную\n" +
                "Управление ставками CPM \uD83D\uDCB0: бот будет  автоматически регулировать ставки для  оптимального размещения рекламы.\n Настройка цен и скидок \uD83C\uDFF7: введите  желаемую цену на товар, и бот рассчитает  необходимую скидку. \nПополнение рекламного кабинета \uD83D\uDCF2:  запросите QR-код для пополнения, и бот  выдаст его для оплаты.\n Оптимальное размещение рекламы \uD83D\uDCC8:  бот обеспечит лучшее размещение ваших  объявлений.";
    }

    @Override
    public ClientState byState() {
        return ClientState.INSTRUCTIONS_MENU;
    }
}
