package ru.akvine.marketspace.bot.telegram.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.services.ClientService;

@RequiredArgsConstructor
@Slf4j
public class ClientBlockedFilter extends MessageFilter {
    private final ClientService clientService;

    @Override
    public BotApiMethod<?> handle(Update update) {
        String chatId = getChatId(update);
        logger.debug("Update data was reached in ClientBlockedFilter for chat with id = {}", chatId);
        clientService.checkIsBlocked(chatId);
        return nextMessageFilter.handle(update);
    }
}
