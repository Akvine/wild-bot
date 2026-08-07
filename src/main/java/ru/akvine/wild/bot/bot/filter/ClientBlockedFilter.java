package ru.akvine.wild.bot.bot.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.services.ClientService;

@RequiredArgsConstructor
@Slf4j
public class ClientBlockedFilter extends MessageFilter {
    private final ClientService clientService;

    @Override
    public Response handle(Payload payload) {
        String chatId = payload.getChatId();
        logger.debug("Update data was reached in ClientBlockedFilter for chat with id = {}", chatId);
        clientService.checkIsBlocked(chatId);
        return nextMessageFilter.handle(payload);
    }
}
