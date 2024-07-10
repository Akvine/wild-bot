package ru.akvine.marketspace.bot.resolvers.data;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.enums.TelegramDataType;

public interface TelegramDataResolver {
    String extractChatId(Update update);

    String extractText(Update update);

    TelegramDataType getType();
}
