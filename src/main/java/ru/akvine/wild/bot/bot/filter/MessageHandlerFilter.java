package ru.akvine.wild.bot.bot.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.MessageDispatcher;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;

@Component
@RequiredArgsConstructor
public class MessageHandlerFilter extends MessageFilter {
    private final MessageDispatcher messageDispatcher;

    @Override
    public Response handle(Payload payload) {
        return messageDispatcher.doDispatch(payload);
    }
}
