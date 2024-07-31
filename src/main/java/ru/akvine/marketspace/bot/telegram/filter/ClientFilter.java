package ru.akvine.marketspace.bot.telegram.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.services.ClientService;
import ru.akvine.marketspace.bot.services.domain.ClientBean;
import ru.akvine.marketspace.bot.services.dto.ClientCreate;

@RequiredArgsConstructor
@Slf4j
public class ClientFilter extends MessageFilter {
    private final ClientService clientService;

    @Override
    public BotApiMethod<?> handle(Update update) {
        String chatId = getChatId(update);

        logger.debug("Update data was reached in ClientFilter for chat with id = {}", chatId);
        ClientBean clientBean = clientService.findByChatId(chatId);
        if (clientBean == null) {
            Message message = update.getMessage();
            ClientCreate clientCreate = new ClientCreate(
                    chatId,
                    message.getFrom().getUserName(),
                    message.getFrom().getFirstName(),
                    message.getFrom().getLastName()
            );
            clientBean = clientService.create(clientCreate);
        } else {
            if (clientBean.isDeleted()) {
                return new SendMessage(chatId, "Ваш профиль был удален. Пожалуйста, обратитесь в поддержку");
            }
        }
        MDC.put("username", clientBean.getUsername());
        return nextMessageFilter.handle(update);
    }
}
