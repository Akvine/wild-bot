package ru.akvine.wild.bot.bot;

import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;

public interface MessageDispatcher {
    Response doDispatch(Payload payload);
}
