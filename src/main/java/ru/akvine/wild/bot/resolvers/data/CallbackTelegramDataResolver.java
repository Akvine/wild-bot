package ru.akvine.wild.bot.resolvers.data;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.enums.TelegramDataType;

@Component
public class CallbackTelegramDataResolver implements TelegramDataResolver {
    @Override
    public String extractChatId(Update update) {
        return String.valueOf(update.getCallbackQuery().getMessage().getChatId());
    }

    @Override
    public String extractText(Update update) {
        return update.getCallbackQuery().getData();
    }

    @Override
    public TelegramDataType getType() {
        return TelegramDataType.CALLBACK;
    }
}
