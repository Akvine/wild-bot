package ru.akvine.marketspace.bot.telegram.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.exceptions.*;

import static ru.akvine.marketspace.bot.constants.TelegramMessageConstants.CLIENT_NOT_IN_WHITELIST_MESSAGE;

@RequiredArgsConstructor
@Slf4j
public class MessageExceptionFilter extends MessageFilter {
    @Value("${telegram.bot.support.username}")
    private String supportUsername;

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
            if (exception instanceof AdvertStartException) {
                return processStartAdvertException(chatId, (AdvertStartException) exception);
            }
            if (exception instanceof ClientNotInWhitelistException) {
                return processClientWhitelistException(chatId, exception.getMessage());
            }
            if (exception instanceof AdvertStartLimitException) {
                processAdvertStartLimitException(chatId);
            }
            return processGeneralException(chatId, exception);
        }
    }

    private SendMessage processGeneralException(String chatId, Exception exception) {
        logger.error("Some error occurred for chatId = {}, ex = {}", chatId, exception.getMessage());
        String messageToUser = String.format(
                "Произошла неизвестная ошибка... Пожалуйста, обратитесь в поддержку: %s",
                supportUsername
        );
        return new SendMessage(chatId, messageToUser);
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

    private SendMessage processStartAdvertException(String chatId, AdvertStartException exception) {
        logger.warn("Error while starting advert, message = {}", exception.getMessage());
        String errorMessage = String.format(
                "При запуске рекламной кампании произошла ошибка. Пожалуйста, обратитесь в поддержку: %s",
                supportUsername
        );
        return new SendMessage(chatId, errorMessage);
    }

    private SendMessage processClientWhitelistException(String chatId, String message) {
        logger.info("Client with chat id = {} not in whitelist. Message = {}", chatId, message);
        return new SendMessage(chatId, CLIENT_NOT_IN_WHITELIST_MESSAGE);
    }

    private SendMessage processAdvertStartLimitException(String chatId) {
        logger.info("Start advert limit reached for client with chat id = {}", chatId);
        return new SendMessage(chatId, "Превышен лимит по запуску рекламных кампаний!");
    }
}
