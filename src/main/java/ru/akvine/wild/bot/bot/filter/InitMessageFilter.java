package ru.akvine.wild.bot.bot.filter;

import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;

public interface InitMessageFilter {
    Response handle(Payload payload);
}
