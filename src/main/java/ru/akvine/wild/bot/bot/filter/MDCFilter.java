package ru.akvine.wild.bot.bot.filter;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.constants.MDCConstants;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.services.ClientService;

@Component
@RequiredArgsConstructor
public class MDCFilter extends MessageFilter {
    private final ClientService clientService;

    @Override
    public Response handle(Payload payload) {
        String chatId = payload.getChatId();
        BotType botType = payload.getBotType();

        try {
            String username = clientService.getByChatIdAndBotType(chatId, botType).getUsername();
            if (username != null) {
                MDC.put(MDCConstants.USERNAME, username);
            }
            MDC.put(MDCConstants.CHAT_ID, chatId);
            MDC.put(MDCConstants.BOT_TYPE, payload.getBotType().getType());
            return nextMessageFilter.handle(payload);
        } finally {
            MDC.clear();
        }
    }
}
