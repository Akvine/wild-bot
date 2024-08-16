package ru.akvine.marketspace.bot.resolvers.controllers.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.infrastructure.state.StateStorage;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.managers.TelegramViewManager;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.telegram.TelegramData;

import java.util.List;

import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.QUERY_QR_CODE_BUTTON_TEXT;

@Component
public class FillAdvertisingStateResolver extends StateResolver {

    @Autowired
    public FillAdvertisingStateResolver(TelegramDataResolverManager dataResolverManager,
                                        StateStorage<String, List<ClientState>> stateStorage,
                                        TelegramViewManager telegramViewManager) {
        super(stateStorage, telegramViewManager, dataResolverManager);
    }

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        super.resolve(telegramData);
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        String text = resolver.extractText(telegramData.getData());

        if (text.equals(QUERY_QR_CODE_BUTTON_TEXT)) {
            return new SendMessage(chatId, "Спасибо! В ближайшее время бот отправит вам QR-код на пополнение бюджета :-)");
        } else {
            return new SendMessage(chatId, "Вывберите действие из меню");
        }
    }

    @Override
    public ClientState getState() {
        return ClientState.FILL_ADVERTISING_ACCOUNT_MENU;
    }
}
