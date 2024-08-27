package ru.akvine.marketspace.bot.telegram.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.exceptions.TelegramHandlingException;
import ru.akvine.marketspace.bot.exceptions.telegram.TelegramExceptionHandler;
import ru.akvine.marketspace.bot.infrastructure.annotations.TelegramErrorHandler;

import java.lang.reflect.Method;

@RequiredArgsConstructor
@Slf4j
public class TelegramExceptionFilter extends MessageFilter {
    @Value("${telegram.bot.support.url}")
    private String supportUrl;

    private final TelegramExceptionHandler telegramExceptionHandler;

    @Override
    public BotApiMethod<?> handle(Update update) {
        String chatId = getChatId(update);
        try {
            logger.debug("Update data was reached in TelegramException filter for chat with id = {}", chatId);
            return nextMessageFilter.handle(update);
        } catch (Exception exception) {
            Method[] methods = telegramExceptionHandler.getClass().getDeclaredMethods();
            for (Method method : methods) {
                TelegramErrorHandler errorAnnotation = method.getAnnotation(TelegramErrorHandler.class);
                try {
                    if (errorAnnotation != null && errorAnnotation.value().isInstance(exception)) {
                        return (SendMessage) method.invoke(telegramExceptionHandler, chatId, exception);
                    }
                } catch (Exception invokeException) {
                    logger.error("Error while handling exception. Message: {}. StackTrace: {}",
                            invokeException.getMessage(), invokeException.getStackTrace());
                    throw new TelegramHandlingException(invokeException);
                }
            }

            return processGeneralException(chatId, exception);
        }
    }

    private SendMessage processGeneralException(String chatId, Exception exception) {
        logger.error("Some error occurred. Message = [{}]. StackTrace: {}", exception.getMessage(), exception.getStackTrace());
        String messageToUser = String.format(
                "Произошла неизвестная ошибка :( \nПожалуйста, обратитесь в поддержку: %s",
                supportUrl
        );
        return new SendMessage(chatId, messageToUser);
    }

}
