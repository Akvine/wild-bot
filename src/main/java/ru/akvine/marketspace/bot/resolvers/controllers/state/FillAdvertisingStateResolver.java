package ru.akvine.marketspace.bot.resolvers.controllers.state;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.telegram.TelegramData;

import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.QUERY_QR_CODE_BUTTON_TEXT;

@Component
@RequiredArgsConstructor
public class FillAdvertisingStateResolver implements StateResolver {
    private final TelegramDataResolverManager dataResolverManager;

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
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
