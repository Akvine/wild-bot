package ru.akvine.wild.bot.resolvers.data;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.enums.TelegramDataType;

public interface TelegramDataResolver {
    String extractChatId(Update update);

    String extractText(Update update);

    TelegramDataType getType();
}
