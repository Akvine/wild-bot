package ru.akvine.marketspace.bot.exceptions.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.exceptions.*;
import ru.akvine.marketspace.bot.infrastructure.annotations.TelegramErrorHandler;

import static ru.akvine.marketspace.bot.constants.TelegramMessageErrorConstants.CLIENT_SUBSCRIPTION_MESSAGE;

@Component
@Slf4j
public class TelegramExceptionHandler {
    @Value("${telegram.bot.support.url}")
    private String supportUrl;
    @Value("${photo.width.min.pixels}")
    private int minWidth;
    @Value("${photo.height.min.pixels}")
    private int minHeight;
    @Value("${photo.max.size.megabytes}")
    private int maxMegabytesSize;

    @TelegramErrorHandler(BlockedCredentialsException.class)
    public SendMessage handleBlockedCredentialsException(String chatId, BlockedCredentialsException exception) {
        logger.info("Client is blocked. Message = [{}]", exception.getMessage());
        return new SendMessage(chatId, exception.getMessage());
    }

    @TelegramErrorHandler(AdvertNotFoundException.class)
    public SendMessage handleAdvertNotFoundException(String chatId, AdvertNotFoundException exception) {
        logger.warn("Has no advert. Message = [{}]", exception.getMessage());
        return new SendMessage(chatId, "Рекламная кампания для запуска не найдена. Попробуйте позже...");
    }

    @TelegramErrorHandler(AdvertStartException.class)
    public SendMessage handleAdvertStartException(String chatId, AdvertStartException exception) {
        logger.warn("Error while starting advert, message = {}", exception.getMessage());
        String errorMessage = String.format(
                "При запуске рекламной кампании произошла ошибка. \nПожалуйста, обратитесь в поддержку: %s",
                supportUrl
        );
        return new SendMessage(chatId, errorMessage);
    }

    @TelegramErrorHandler(SubscriptionException.class)
    public SendMessage handleClientSubscriptionException(String chatId, SubscriptionException exception) {
        logger.info("Client's subscription is expired or has no. Message = [{}]", exception.getMessage());
        return new SendMessage(chatId, CLIENT_SUBSCRIPTION_MESSAGE);
    }

    @TelegramErrorHandler(AdvertStartLimitException.class)
    public SendMessage handleAdvertStartLimitException(String chatId, AdvertStartLimitException exception) {
        logger.info("Start advert limit is reached");
        return new SendMessage(chatId, "Превышен лимит по запуску рекламных кампаний!");
    }

    @TelegramErrorHandler(PhotoDimensionsValidationException.class)
    public SendMessage handlePhotoDimensionsValidationException(String chatId, PhotoDimensionsValidationException exception) {
        logger.info("Photo dimensions validation error. Message = [{}]", exception.getMessage());
        String message = String.format(
                "Неверный размер изображения!\nМинимум: %sx%s. Фактический: %sx%s",
                minWidth, minHeight, exception.getWidth(), exception.getHeight()
        );
        return new SendMessage(chatId, message);
    }

    @TelegramErrorHandler(PhotoSizeValidationException.class)
    public SendMessage handlePhotoSizeValidationException(String chatId, PhotoSizeValidationException exception) {
        double currentMegabytes = exception.getMegabytes();
        logger.info("Photo size validation error. Message = [{}]", exception.getMessage());
        String message = String.format(
                "Размер изображения слишком большой!\nМаксимум %s. Фактический: %s",
                maxMegabytesSize, currentMegabytes
        );
        return new SendMessage(chatId, message);
    }
}
