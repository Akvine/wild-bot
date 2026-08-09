package ru.akvine.wild.bot.exceptions.telegram;

import static ru.akvine.wild.bot.constants.telegram.BotMessageErrorConstants.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.exceptions.*;
import ru.akvine.wild.bot.infrastructure.annotations.ErrorHandler;

/**
 * Превращает бизнес-исключения, всплывшие при обработке сообщения ({@code BotExceptionFilter}),
 * в понятный клиенту ответ бота вместо падения с общей "неизвестной ошибкой". Каждый метод
 * помечен {@link ErrorHandler}, где в качестве значения указан класс перехватываемого
 * исключения — {@code BotExceptionFilter} рефлексией находит по этой аннотации подходящий
 * метод для конкретного пойманного исключения и вызывает его. Если подходящего обработчика
 * не нашлось, {@code BotExceptionFilter} сам формирует общий ответ об ошибке, минуя этот класс.
 * Работает одинаково для всех типов бота (Telegram, MAX) — конкретная платформа передаётся
 * параметром {@code botType} в каждый метод.
 */
@Component
@Slf4j
public class BotExceptionHandler {
    @Value("${telegram.bot.support.url}")
    private String supportUrl;

    @Value("${photo.width.min.pixels}")
    private int minWidth;

    @Value("${photo.height.min.pixels}")
    private int minHeight;

    @Value("${photo.max.size.megabytes}")
    private int maxMegabytesSize;

    @ErrorHandler(BlockedCredentialsException.class)
    public Response handleBlockedCredentialsException(
            String chatId, BotType botType, BlockedCredentialsException exception) {
        logger.info("Client is blocked. Message = [{}]", exception.getMessage());
        return new Response(chatId, exception.getMessage(), botType);
    }

    @ErrorHandler(AdvertNotFoundException.class)
    public Response handleAdvertNotFoundException(String chatId, BotType botType, AdvertNotFoundException exception) {
        logger.warn("Has no advert. Message = [{}]", exception.getMessage());
        return new Response(chatId, "Рекламная кампания для запуска не найдена. Попробуйте позже...", botType);
    }

    @ErrorHandler(AdvertStartException.class)
    public Response handleAdvertStartException(String chatId, BotType botType, AdvertStartException exception) {
        logger.warn("Error while starting advert, message = {}", exception.getMessage());
        String errorMessage = String.format(
                "При запуске рекламной кампании произошла ошибка. \nПожалуйста, обратитесь в поддержку: %s",
                supportUrl);
        return new Response(chatId, errorMessage, botType);
    }

    @ErrorHandler(HasNoSubscriptionException.class)
    public Response handleClientSubscriptionException(
            String chatId, BotType botType, HasNoSubscriptionException exception) {
        logger.info("Client has no subscription. Message = [{}]", exception.getMessage());
        return new Response(chatId, CLIENT_HAS_NO_SUBSCRIPTION_MESSAGE, botType);
    }

    @ErrorHandler(AdvertStartLimitException.class)
    public Response handleAdvertStartLimitException(
            String chatId, BotType botType, AdvertStartLimitException exception) {
        logger.info("Start advert limit is reached");
        return new Response(chatId, "Превышен лимит по запуску рекламных кампаний!", botType);
    }

    @ErrorHandler(PhotoDimensionsValidationException.class)
    public Response handlePhotoDimensionsValidationException(
            String chatId, BotType botType, PhotoDimensionsValidationException exception) {
        logger.info("Photo dimensions validation error. Message = [{}]", exception.getMessage());
        String message = String.format(
                "Неверный размер изображения!\nМинимум: %sx%s. Фактический: %sx%s",
                minWidth, minHeight, exception.getWidth(), exception.getHeight());
        return new Response(chatId, message, botType);
    }

    @ErrorHandler(PhotoSizeValidationException.class)
    public Response handlePhotoSizeValidationException(
            String chatId, BotType botType, PhotoSizeValidationException exception) {
        double currentMegabytes = exception.getMegabytes();
        logger.info("Photo size validation error. Message = [{}]", exception.getMessage());
        String message = String.format(
                "Размер изображения слишком большой!\nМаксимум %s. Фактический: %s",
                maxMegabytesSize, currentMegabytes);
        return new Response(chatId, message, botType);
    }

    @ErrorHandler(WhitelistException.class)
    public Response handleWhitelistException(String chatId, BotType botType, WhitelistException exception) {
        logger.info("Client not in whitelist");
        return new Response(chatId, CLIENT_NOT_IN_WHITELIST_MESSAGE, botType);
    }

    @ErrorHandler(SubscriptionExpiredException.class)
    public Response handleSubscriptionExpiredException(
            String chatId, BotType botType, SubscriptionExpiredException exception) {
        logger.info("Client's subscription is expired");
        return new Response(chatId, CLIENT_SUBSCRIPTION_EXPIRED_MESSAGE, botType);
    }
}
