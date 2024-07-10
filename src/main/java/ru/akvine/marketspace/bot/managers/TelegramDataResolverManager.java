package ru.akvine.marketspace.bot.managers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.marketspace.bot.enums.TelegramDataType;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;

import java.util.Map;

@Getter
@AllArgsConstructor
public class TelegramDataResolverManager {
    private Map<TelegramDataType, TelegramDataResolver> telegramDataResolvers;
}
