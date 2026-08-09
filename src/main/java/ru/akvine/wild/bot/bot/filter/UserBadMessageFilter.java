package ru.akvine.wild.bot.bot.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.akvine.wild.bot.bot.MessageDispatcher;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.exceptions.bot.InvalidUserMessageException;
import ru.akvine.wild.bot.utils.BotMessageUtils;

@RequiredArgsConstructor
@Slf4j
public class UserBadMessageFilter extends MessageFilter {
    private final MessageDispatcher messageDispatcher;

    @Override
    public Response handle(Payload payload) {
        String chatId = payload.getChatId();
        logger.debug(
                "Payload data was reached in " + UserBadMessageFilter.class.getSimpleName()
                        + " filter for chat with id = {}",
                chatId);

        if (BotMessageUtils.isOnlySticker(payload.getMessage())) {
            throw new InvalidUserMessageException("User input invalid message");
        }

        return messageDispatcher.doDispatch(payload);
    }
}
