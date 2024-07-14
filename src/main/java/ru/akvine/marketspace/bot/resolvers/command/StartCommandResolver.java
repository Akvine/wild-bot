package ru.akvine.marketspace.bot.resolvers.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.controller.AdvertStartController;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.enums.Command;
import ru.akvine.marketspace.bot.infrastructure.SessionStorage;
import ru.akvine.marketspace.bot.infrastructure.StateStorage;
import ru.akvine.marketspace.bot.infrastructure.impl.ClientSessionData;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartCommandResolver implements CommandResolver {
    private final StateStorage<String> stateStorage;
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final AdvertStartController advertStartController;

    @Value("${advert.start.enabled}")
    private boolean isAdvertStartEnabled;

    @Override
    public BotApiMethod<?> resolve(String chatId, String text) {
        logger.info("[{}] resolved for chat with id = {} and text = {}", getCommand(), chatId, text);

        if (isAdvertStartEnabled) {
            SendMessage sendMessage = advertStartController.getCategories(chatId);
            stateStorage.setState(chatId, ClientState.CHOOSE_CATEGORY_STATE);
            sessionStorage.init(chatId);
            return sendMessage;
        } else {
            return new SendMessage(chatId,
                    "Запуск новых рекламных кампаний в данный момент \nв связи с тех. обслуживанием невозможен.\nПопробуйте позже.\n" +
                            "С уважением, команда \"marketspace-bot\""
            );
        }
    }

    @Override
    public Command getCommand() {
        return Command.COMMAND_START;
    }
}
