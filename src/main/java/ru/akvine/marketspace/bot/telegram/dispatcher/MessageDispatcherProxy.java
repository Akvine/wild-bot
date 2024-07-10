package ru.akvine.marketspace.bot.telegram.dispatcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.exceptions.AdvertNotFoundException;
import ru.akvine.marketspace.bot.exceptions.BlockedCredentialsException;
import ru.akvine.marketspace.bot.exceptions.ClientWhitelistException;
import ru.akvine.marketspace.bot.exceptions.StartAdvertException;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageDispatcherProxy implements MessageDispatcher {
    private final MessageDispatcher messageDispatcher;

    public BotApiMethod<?> doDispatch(Update update) {
        String chatId;
        if (update.getCallbackQuery() != null) {
            chatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
        } else {
            chatId = String.valueOf(update.getMessage().getChatId());
        }

        try {
            return messageDispatcher.doDispatch(update);
        } catch (Exception exception) {
            if (exception instanceof BlockedCredentialsException) {
                return processBlockedCredentialsException(chatId, exception.getMessage());
            }
            if (exception instanceof AdvertNotFoundException) {
                return processAdvertNotFoundException(chatId);
            }
            if (exception instanceof StartAdvertException) {
                return processStartAdvertException(chatId, exception.getMessage());
            }
            if (exception instanceof ClientWhitelistException) {
                return processClientWhitelistException(chatId);
            }
            return processGeneralException(chatId, exception);
        }
    }

    private SendMessage processGeneralException(String chatId, Exception exception) {
        logger.error("Some error occurred for chatId = {}, ex = {}", chatId, exception.getMessage());
        return new SendMessage(chatId, "Произошла неизвестная ошибка...");
    }

    private SendMessage processBlockedCredentialsException(String chatId, String message) {
        return new SendMessage(chatId, message);
    }

    private SendMessage processAdvertNotFoundException(String chatId) {
        return new SendMessage(chatId, "Не найдено ни одной рекламной кампании в статусе \"На паузе\" или \"Готова к запуску\"");
    }

    private SendMessage processStartAdvertException(String chatId, String message) {
        return new SendMessage(chatId, message);
    }

    private SendMessage processClientWhitelistException(String chatId) {
        return new SendMessage(chatId, "Вы не можете использовать функционал бота, т.к. не в whitelist");
    }
}
