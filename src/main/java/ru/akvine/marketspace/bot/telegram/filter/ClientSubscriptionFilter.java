package ru.akvine.marketspace.bot.telegram.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.exceptions.ClientSubscriptionException;
import ru.akvine.marketspace.bot.services.ClientSubscriptionService;
import ru.akvine.marketspace.bot.services.domain.ClientSubscriptionModel;

@RequiredArgsConstructor
@Slf4j
public class ClientSubscriptionFilter extends MessageFilter {
    private final ClientSubscriptionService clientSubscriptionService;

    @Override
    public BotApiMethod<?> handle(Update update) {
        String chatId = getChatId(update);
        logger.debug("Update data was reached in ClientSubscriptionFilter for chat with id = {}", chatId);
        ClientSubscriptionModel clientSubscription = clientSubscriptionService.getByChatIdOrNull(chatId);
        if (clientSubscription == null) {
            throw new ClientSubscriptionException("Client has no subscription");
        }
        if (clientSubscription.isExpiresAt()) {
            throw new ClientSubscriptionException("Client subscription is expired");
        }
        return nextMessageFilter.handle(update);
    }
}
