package ru.akvine.wild.bot.telegram.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.domain.ClientModel;
import ru.akvine.wild.bot.services.dto.ClientCreate;

@RequiredArgsConstructor
@Slf4j
public class ClientFilter extends MessageFilter {
    private final ClientService clientService;

    @Override
    public BotApiMethod<?> handle(Update update) {
        String chatId = getChatId(update);

        logger.debug("Update data was reached in ClientFilter for chat with id = {}", chatId);
        ClientModel clientBean = clientService.findByChatId(chatId);
        if (clientBean == null) {
            Message message = update.getMessage();
            ClientCreate clientCreate = new ClientCreate(
                    chatId,
                    message.getFrom().getUserName(),
                    message.getFrom().getFirstName(),
                    message.getFrom().getLastName()
            );
            clientService.create(clientCreate);
        } else {
            if (clientBean.isDeleted()) {
                return new SendMessage(chatId, "Ваш профиль был удален. Пожалуйста, обратитесь в поддержку");
            }
        }
        return nextMessageFilter.handle(update);
    }
}
