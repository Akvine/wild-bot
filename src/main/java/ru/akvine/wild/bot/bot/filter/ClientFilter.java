package ru.akvine.wild.bot.bot.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.exceptions.ClientHasBeenDeletedException;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.domain.ClientModel;
import ru.akvine.wild.bot.services.dto.ClientCreate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientFilter extends MessageFilter {
    private final ClientService clientService;

    @Override
    public Response handle(Payload payload) {
        String chatId = payload.getChatId();
        BotType botType = payload.getBotType();

        logger.debug(
                "Payload was reached in ClientFilter for chat with id = [{}]. Type = [{}]",
                chatId,
                payload.getBotType());
        ClientModel clientBean = clientService.findByChatIdAndBotType(chatId, botType);
        if (clientBean == null) {
            ClientCreate clientCreate = new ClientCreate(
                    chatId, botType, payload.getUsername(), payload.getFirstName(), payload.getLastName());
            clientService.create(clientCreate);
        } else {
            if (clientBean.isDeleted()) {
                throw new ClientHasBeenDeletedException("Client has been deleted! Contact with support");
            }
        }
        return nextMessageFilter.handle(payload);
    }
}
