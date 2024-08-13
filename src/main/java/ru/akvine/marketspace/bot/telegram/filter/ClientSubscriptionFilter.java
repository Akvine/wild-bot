package ru.akvine.marketspace.bot.telegram.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.exceptions.SubscriptionException;
import ru.akvine.marketspace.bot.services.SubscriptionService;
import ru.akvine.marketspace.bot.services.domain.SubscriptionModel;

@RequiredArgsConstructor
@Slf4j
public class ClientSubscriptionFilter extends MessageFilter {
    private final SubscriptionService subscriptionService;

    @Override
    public BotApiMethod<?> handle(Update update) {
        String chatId = getChatId(update);
        logger.debug("Update data was reached in ClientSubscriptionFilter for chat with id = {}", chatId);
        SubscriptionModel clientSubscription = subscriptionService.getByChatIdOrNull(chatId);
        if (clientSubscription == null) {
            throw new SubscriptionException("Client has no subscription");
        }
        if (clientSubscription.isExpired()) {
            throw new SubscriptionException("Client subscription is expired");
        }
        return nextMessageFilter.handle(update);
    }
}
