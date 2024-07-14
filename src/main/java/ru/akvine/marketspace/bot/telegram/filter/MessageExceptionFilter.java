package ru.akvine.marketspace.bot.telegram.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.exceptions.AdvertNotFoundException;
import ru.akvine.marketspace.bot.exceptions.BlockedCredentialsException;
import ru.akvine.marketspace.bot.exceptions.ClientNotInWhitelistException;
import ru.akvine.marketspace.bot.exceptions.StartAdvertException;

@RequiredArgsConstructor
@Slf4j
public class MessageExceptionFilter extends MessageFilter {

    @Override
    public BotApiMethod<?> handle(Update update) {
        String chatId = getChatId(update);
        try {
            logger.debug("Update data was reached in MessageException filter for chat with id = {}", chatId);
            return nextMessageFilter.handle(update);
        } catch (Exception exception) {
            if (exception instanceof BlockedCredentialsException) {
                return processBlockedCredentialsException(chatId, exception.getMessage());
            }
            if (exception instanceof AdvertNotFoundException) {
                return processAdvertNotFoundException(chatId, exception.getMessage());
            }
            if (exception instanceof StartAdvertException) {
                return processStartAdvertException(chatId, exception.getMessage());
            }
            if (exception instanceof ClientNotInWhitelistException) {
                return processClientWhitelistException(chatId, exception.getMessage());
            }
            return processGeneralException(chatId, exception);
        }
    }

    private SendMessage processGeneralException(String chatId, Exception exception) {
        logger.error("Some error occurred for chatId = {}, ex = {}", chatId, exception.getMessage());
        return new SendMessage(chatId, "Произошла неизвестная ошибка...");
    }

    private SendMessage processBlockedCredentialsException(String chatId, String message) {
        logger.info("Client with chat id = {} is blocked. Message = {}", chatId, message);
        return new SendMessage(chatId, message);
    }

    private SendMessage processAdvertNotFoundException(String chatId, String exceptionMessage) {
        logger.warn(
                "For chat with id = {} has no advert in status \"PAUSE\" or \"READY_TO_START\". Message = {}",
                chatId, exceptionMessage);
        return new SendMessage(chatId, "Не найдено ни одной рекламной кампании в статусе \"На паузе\" или \"Готова к запуску\"");
    }

    private SendMessage processStartAdvertException(String chatId, String message) {

        return new SendMessage(chatId, message);
    }

    private SendMessage processClientWhitelistException(String chatId, String message) {
        logger.info("Client with chat id = {} not in whitelist. Message = {}", chatId, message);
        return new SendMessage(chatId, "Вы не можете использовать функционал бота, т.к. не в whitelist");
    }
}
