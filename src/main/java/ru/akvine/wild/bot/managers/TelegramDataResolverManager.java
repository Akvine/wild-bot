package ru.akvine.wild.bot.managers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.enums.TelegramDataType;
import ru.akvine.wild.bot.resolvers.data.TelegramDataResolver;

import java.util.Map;

@Getter
@AllArgsConstructor
public class TelegramDataResolverManager {
    private Map<TelegramDataType, TelegramDataResolver> telegramDataResolvers;
}
