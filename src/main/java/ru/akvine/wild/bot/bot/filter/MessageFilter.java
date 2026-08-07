package ru.akvine.wild.bot.bot.filter;

import lombok.Setter;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;

@Setter
public abstract class MessageFilter {
    protected MessageFilter nextMessageFilter;

    public abstract Response handle(Payload payload);
}
