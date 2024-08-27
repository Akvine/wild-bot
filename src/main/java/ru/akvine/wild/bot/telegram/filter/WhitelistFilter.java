package ru.akvine.wild.bot.telegram.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.exceptions.WhitelistException;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.domain.ClientModel;

@RequiredArgsConstructor
@Slf4j
public class WhitelistFilter extends MessageFilter {
    private final ClientService clientService;

    @Override
    public BotApiMethod<?> handle(Update update) {
        String chatId = getChatId(update);
        logger.debug("Update data was reached in WhitelistFilter for chat with id = {}", chatId);
        ClientModel clientModel = clientService.getByChatId(chatId);
        if (!clientModel.isInWhitelist()) {
            throw new WhitelistException("Client not in whitelist!");
        }
        return nextMessageFilter.handle(update);
    }
}
