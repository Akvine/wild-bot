package ru.akvine.wild.bot.bot.filter;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.constants.MDCConstants;
import ru.akvine.wild.bot.services.ClientService;

@RequiredArgsConstructor
public class MDCFilter extends MessageFilter {
    private final ClientService clientService;

    @Override
    public Response handle(Payload payload) {
        String chatId = payload.getChatId();
        String username = clientService.getByChatId(chatId).getUsername();
        if (username != null) {
            MDC.put(MDCConstants.USERNAME, username);
        }
        MDC.put(MDCConstants.CHAT_ID, chatId);
        MDC.put(MDCConstants.BOT_TYPE, payload.getBotType().getType());
        Response response = nextMessageFilter.handle(payload);
        MDC.clear();
        return response;
    }
}
