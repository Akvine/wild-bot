package ru.akvine.wild.bot.bot.filter;

import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.exceptions.BotHandlingException;
import ru.akvine.wild.bot.exceptions.telegram.BotExceptionHandler;
import ru.akvine.wild.bot.infrastructure.annotations.ErrorHandler;
import ru.akvine.wild.bot.services.integration.max.dto.MaxSendMessage;

@RequiredArgsConstructor
@Slf4j
public class BotExceptionFilter extends MessageFilter {
    @Value("${telegram.bot.support.url}")
    private String supportUrl;

    private final BotExceptionHandler botExceptionHandler;

    @Override
    public Response handle(Payload payload) {
        String chatId = payload.getChatId();
        BotType botType = payload.getBotType();
        try {
            logger.debug(
                    "Payload data was reached in " + BotExceptionFilter.class.getSimpleName()
                            + " filter for chat with id = {}, bot type = {}",
                    chatId,
                    payload.getBotType());
            return nextMessageFilter.handle(payload);
        } catch (Exception exception) {
            Method[] methods = botExceptionHandler.getClass().getDeclaredMethods();
            for (Method method : methods) {
                ErrorHandler errorAnnotation = method.getAnnotation(ErrorHandler.class);
                try {
                    if (errorAnnotation != null && errorAnnotation.value().isInstance(exception)) {
                        return (Response) method.invoke(botExceptionHandler, chatId, botType, exception);
                    }
                } catch (Exception invokeException) {
                    logger.error(
                            "Error while handling exception. Message: {}. StackTrace: {}",
                            invokeException.getMessage(),
                            invokeException.getStackTrace());
                    throw new BotHandlingException(invokeException);
                }
            }

            return processGeneralException(payload.getBotType(), chatId, exception);
        }
    }

    private Response processGeneralException(BotType botType, String chatId, Exception exception) {
        logger.error(
                "Some error occurred. Message = [{}]. StackTrace: {}",
                exception.getMessage(),
                exception.getStackTrace());
        Response response = new Response();

        String messageToUser;
        if (botType == BotType.TELEGRAM) {
            messageToUser = String.format(
                    "Произошла неизвестная ошибка :( \nПожалуйста, обратитесь в поддержку: %s", supportUrl);
            return response.setTelegramResponse(new SendMessage(chatId, messageToUser));
        }

        messageToUser = "Произошла неизвестная ошибка :( \nПожалуйста, обратитесь в поддержку";
        return response.setMaxSendMessage(new MaxSendMessage().setChatId(chatId).setText(messageToUser));
    }
}
