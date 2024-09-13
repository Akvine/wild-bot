package ru.akvine.wild.bot.facades;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.enums.TelegramDataType;
import ru.akvine.wild.bot.resolvers.data.TelegramDataResolver;

import java.util.Map;

@Getter
@AllArgsConstructor
public class TelegramDataResolverFacade {
    private Map<TelegramDataType, TelegramDataResolver> telegramDataResolvers;
}
