package ru.akvine.wild.bot.bot.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.exceptions.WhitelistException;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.domain.ClientModel;

@RequiredArgsConstructor
@Slf4j
public class WhitelistFilter extends MessageFilter {
    private final ClientService clientService;

    @Override
    public Response handle(Payload payload) {
        String chatId = payload.getChatId();
        BotType botType = payload.getBotType();

        logger.debug(
                "Update data was reached in WhitelistFilter for chat with id = {}, bot type = {}",
                chatId,
                payload.getBotType());
        ClientModel clientModel = clientService.getByChatIdAndBotType(chatId, botType);
        if (!clientModel.isInWhitelist()) {
            throw new WhitelistException("Client not in whitelist!");
        }
        return nextMessageFilter.handle(payload);
    }
}
