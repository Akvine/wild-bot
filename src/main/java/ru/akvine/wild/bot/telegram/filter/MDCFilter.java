package ru.akvine.wild.bot.telegram.filter;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.constants.MDCConstants;
import ru.akvine.wild.bot.services.ClientService;

@RequiredArgsConstructor
public class MDCFilter extends MessageFilter {
    private final ClientService clientService;

    @Override
    public BotApiMethod<?> handle(Update update) {
        String chatId = getChatId(update);
        String username = clientService.getByChatId(chatId).getUsername();
        MDC.put(MDCConstants.USERNAME, username);
        MDC.put(MDCConstants.CHAT_ID, chatId);
        BotApiMethod<?> response = nextMessageFilter.handle(update);
        MDC.clear();
        return response;
    }
}
