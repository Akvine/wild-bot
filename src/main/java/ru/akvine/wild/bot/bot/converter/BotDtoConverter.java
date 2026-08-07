package ru.akvine.wild.bot.bot.converter;

import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;

/**
 * Сервис для преобразования DTO
 */
public interface BotDtoConverter<Req, Res> {
    Payload fromRequest(Req request);

    Res toResponse(Response response);

    BotType getType();
}
