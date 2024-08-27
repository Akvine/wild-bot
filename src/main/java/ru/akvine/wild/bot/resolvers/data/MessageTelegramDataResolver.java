package ru.akvine.wild.bot.resolvers.data;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.enums.TelegramDataType;

@Component
public class MessageTelegramDataResolver implements TelegramDataResolver {
    @Override
    public String extractChatId(Update update) {
        return String.valueOf(update.getMessage().getChatId());
    }

    @Override
    public String extractText(Update update) {
        return update.getMessage().getText();
    }

    @Override
    public TelegramDataType getType() {
        return TelegramDataType.MESSAGE;
    }
}
