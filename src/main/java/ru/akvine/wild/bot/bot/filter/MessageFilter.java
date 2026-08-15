package ru.akvine.wild.bot.bot.filter;

import lombok.Setter;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;

@Setter
public abstract class MessageFilter implements InitMessageFilter {
    protected MessageFilter nextMessageFilter;

    public abstract Response handle(Payload payload);

    public final String getFilterClassName() {
        return this.getClass().getSimpleName();
    }
}
