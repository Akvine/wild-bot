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

import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.PAY_SUBSCRIPTION_BUTTON_TEXT;

@Component
public class SubscriptionMenuStateResolver extends StateResolver {
    private final TelegramDataResolverManager dataResolverManager;

    @Autowired
    public SubscriptionMenuStateResolver(StateStorage<String, List<ClientState>> stateStorage, TelegramViewManager viewManager, TelegramDataResolverManager dataResolverManager) {
        super(stateStorage, viewManager, dataResolverManager);
        this.dataResolverManager = dataResolverManager;
    }

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        super.resolve(telegramData);
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        String text = resolver.extractText(telegramData.getData());

        if (text.equals(PAY_SUBSCRIPTION_BUTTON_TEXT)) {
            return new SendMessage(chatId, "Возможность оформления подписки пока не добавлена :(");
        } else {
            return new SendMessage(chatId, "Нужно выбрать действие из меню");
        }
    }

    @Override
    public ClientState getState() {
        return ClientState.SUBSCRIBE_MENU;
    }
}
