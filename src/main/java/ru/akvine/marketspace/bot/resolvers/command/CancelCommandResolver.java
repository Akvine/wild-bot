package ru.akvine.marketspace.bot.resolvers.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.enums.Command;
import ru.akvine.marketspace.bot.infrastructure.SessionStorage;
import ru.akvine.marketspace.bot.infrastructure.StateStorage;
import ru.akvine.marketspace.bot.infrastructure.impl.ClientSessionData;
import ru.akvine.marketspace.bot.services.AdvertService;
import ru.akvine.marketspace.bot.services.domain.AdvertBean;

import static ru.akvine.marketspace.bot.constants.TelegramMessageConstants.DEFAULT_MESSAGE;

@Component
@RequiredArgsConstructor
@Slf4j
public class CancelCommandResolver implements CommandResolver {
    private final StateStorage<String> stateStorage;
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final AdvertService advertService;

    @Override
    public BotApiMethod<?> resolve(String chatId, String text) {
        logger.info("[{}] resolved for chat with id = {} and text = {}", getCommand(), chatId, text);

        stateStorage.removeState(chatId);
        if (StringUtils.isNotBlank(sessionStorage.get(chatId).getLockedAdvertId())) {
            AdvertBean advertBean = advertService.getByAdvertId(sessionStorage.get(chatId).getLockedAdvertId());
            advertBean.setLocked(false);
            advertService.update(advertBean);
        }
        sessionStorage.close(chatId);
        return new SendMessage(chatId, DEFAULT_MESSAGE);
    }

    @Override
    public Command getCommand() {
        return Command.COMMAND_CANCEL;
    }
}
