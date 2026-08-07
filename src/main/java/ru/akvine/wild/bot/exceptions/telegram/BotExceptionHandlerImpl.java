package ru.akvine.wild.bot.exceptions.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.exceptions.*;
import ru.akvine.wild.bot.infrastructure.annotations.ErrorHandler;

import static ru.akvine.wild.bot.constants.telegram.TelegramMessageErrorConstants.*;

/**
 *
 */
@Component
@Slf4j
public class BotExceptionHandlerImpl {
    @Value("${telegram.bot.support.url}")
    private String supportUrl;
    @Value("${photo.width.min.pixels}")
    private int minWidth;
    @Value("${photo.height.min.pixels}")
    private int minHeight;
    @Value("${photo.max.size.megabytes}")
    private int maxMegabytesSize;

    @ErrorHandler(BlockedCredentialsException.class)
    public SendMessage handleBlockedCredentialsException(String chatId, BlockedCredentialsException exception) {
        logger.info("Client is blocked. Message = [{}]", exception.getMessage());
        return new SendMessage(chatId, exception.getMessage());
    }

    @ErrorHandler(AdvertNotFoundException.class)
    public SendMessage handleAdvertNotFoundException(String chatId, AdvertNotFoundException exception) {
        logger.warn("Has no advert. Message = [{}]", exception.getMessage());
        return new SendMessage(chatId, "Рекламная кампания для запуска не найдена. Попробуйте позже...");
    }

    @ErrorHandler(AdvertStartException.class)
    public SendMessage handleAdvertStartException(String chatId, AdvertStartException exception) {
        logger.warn("Error while starting advert, message = {}", exception.getMessage());
        String errorMessage = String.format(
                "При запуске рекламной кампании произошла ошибка. \nПожалуйста, обратитесь в поддержку: %s",
                supportUrl
        );
        return new SendMessage(chatId, errorMessage);
    }

    @ErrorHandler(HasNoSubscriptionException.class)
    public SendMessage handleClientSubscriptionException(String chatId, HasNoSubscriptionException exception) {
        logger.info("Client has no subscription. Message = [{}]", exception.getMessage());
        return new SendMessage(chatId, CLIENT_HAS_NO_SUBSCRIPTION_MESSAGE);
    }

    @ErrorHandler(AdvertStartLimitException.class)
    public SendMessage handleAdvertStartLimitException(String chatId, AdvertStartLimitException exception) {
        logger.info("Start advert limit is reached");
        return new SendMessage(chatId, "Превышен лимит по запуску рекламных кампаний!");
    }

    @ErrorHandler(PhotoDimensionsValidationException.class)
    public SendMessage handlePhotoDimensionsValidationException(String chatId, PhotoDimensionsValidationException exception) {
        logger.info("Photo dimensions validation error. Message = [{}]", exception.getMessage());
        String message = String.format(
                "Неверный размер изображения!\nМинимум: %sx%s. Фактический: %sx%s",
                minWidth, minHeight, exception.getWidth(), exception.getHeight()
        );
        return new SendMessage(chatId, message);
    }

    @ErrorHandler(PhotoSizeValidationException.class)
    public SendMessage handlePhotoSizeValidationException(String chatId, PhotoSizeValidationException exception) {
        double currentMegabytes = exception.getMegabytes();
        logger.info("Photo size validation error. Message = [{}]", exception.getMessage());
        String message = String.format(
                "Размер изображения слишком большой!\nМаксимум %s. Фактический: %s",
                maxMegabytesSize, currentMegabytes
        );
        return new SendMessage(chatId, message);
    }

    @ErrorHandler(WhitelistException.class)
    public SendMessage handleWhitelistException(String chatId, WhitelistException exception) {
        logger.info("Client not in whitelist");
        return new SendMessage(chatId, CLIENT_NOT_IN_WHITELIST_MESSAGE);
    }

    @ErrorHandler(SubscriptionExpiredException.class)
    public SendMessage handleSubscriptionExpiredException(String chatId, SubscriptionExpiredException exception) {
        logger.info("Client's subscription is expired");
        return new SendMessage(chatId, CLIENT_SUBSCRIPTION_EXPIRED_MESSAGE);
    }
}
