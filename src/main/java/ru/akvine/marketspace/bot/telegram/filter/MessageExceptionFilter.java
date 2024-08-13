package ru.akvine.marketspace.bot.telegram.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.exceptions.*;

import static ru.akvine.marketspace.bot.constants.TelegramMessageErrorConstants.CLIENT_SUBSCRIPTION_MESSAGE;

@RequiredArgsConstructor
@Slf4j
public class MessageExceptionFilter extends MessageFilter {
    @Value("${telegram.bot.support.url}")
    private String supportUrl;
    @Value("${photo.width.min.pixels}")
    private int minWidth;
    @Value("${photo.height.min.pixels}")
    private int minHeight;
    @Value("${photo.max.size.megabytes}")
    private int maxMegabytesSize;

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
            if (exception instanceof ClientSubscriptionException) {
                return processClientSubscriptionException(chatId, exception.getMessage());
            }
            if (exception instanceof AdvertStartLimitException) {
                return processAdvertStartLimitException(chatId);
            }
            if (exception instanceof PhotoDimensionsValidationException) {
                return processPhotoDimensionsValidationException(chatId, (PhotoDimensionsValidationException) exception);
            }
            if (exception instanceof PhotoSizeValidationException) {
                return processPhotoSizeValidationException(chatId, (PhotoSizeValidationException) exception);
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

    private SendMessage processBlockedCredentialsException(String chatId, String message) {
        logger.info("Client is blocked. Message = [{}]", message);
        return new SendMessage(chatId, message);
    }

    private SendMessage processAdvertNotFoundException(String chatId, String exceptionMessage) {
        logger.warn("Has no advert. Message = [{}]", exceptionMessage);
        return new SendMessage(chatId, "Рекламная кампания для запуска не найдена. Попробуйте позже...");
    }

    private SendMessage processStartAdvertException(String chatId, AdvertStartException exception) {
        logger.warn("Error while starting advert, message = {}", exception.getMessage());
        String errorMessage = String.format(
                "При запуске рекламной кампании произошла ошибка. \nПожалуйста, обратитесь в поддержку: %s",
                supportUrl
        );
        return new SendMessage(chatId, errorMessage);
    }

    private SendMessage processClientSubscriptionException(String chatId, String message) {
        logger.info("Client's subscription is expired or has no. Message = [{}]", message);
        return new SendMessage(chatId, CLIENT_SUBSCRIPTION_MESSAGE);
    }

    private SendMessage processAdvertStartLimitException(String chatId) {
        logger.info("Start advert limit reached");
        return new SendMessage(chatId, "Превышен лимит по запуску рекламных кампаний!");
    }

    private SendMessage processPhotoDimensionsValidationException(String chatId, PhotoDimensionsValidationException exception) {
        logger.info("Photo dimensions validation error. Message = [{}]", exception.getMessage());
        String message = String.format(
                "Неверный размер изображения!\nМинимум: %sx%s. Фактический: %sx%s",
                minWidth, minHeight, exception.getWidth(), exception.getHeight()
        );
        return new SendMessage(chatId, message);
    }

    private SendMessage processPhotoSizeValidationException(String chatId, PhotoSizeValidationException exception) {
        double currentMegabytes = exception.getMegabytes();
        logger.info("Photo size validation error. Message = [{}]", exception.getMessage());
        String message = String.format(
                "Размер изображения слишком большой!\nМаксимум %s. Фактический: %s",
                maxMegabytesSize, currentMegabytes
        );
        return new SendMessage(chatId, message);
    }
}
