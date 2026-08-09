package ru.akvine.wild.bot.facades;

import java.util.Map;
import lombok.AllArgsConstructor;
import ru.akvine.wild.bot.bot.converter.BotDtoConverter;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.exceptions.bot.BotTypeNotSupportedException;

@AllArgsConstructor
public class BotDtoConverterFacade {
    private Map<BotType, BotDtoConverter<?, ?>> converters;

    public BotDtoConverter getConverter(BotType type) {
        if (converters.containsKey(type)) {
            return converters.get(type);
        }

        throw new BotTypeNotSupportedException("Bot with type = [" + type + "] is not supported by app!");
    }
}
