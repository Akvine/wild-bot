package ru.akvine.wild.bot.services.integration;

import java.util.Set;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.exceptions.RetryException;

/**
 * Класс-адаптер над методами отправки сообщений пользователям в Телеграмме / Максе
 */
public interface BotIntegrationAdapter {

    /**
     * Отправляет изображение указанному чату через бота соответствующего типа.
     *
     * <p>Для {@link BotType#TELEGRAM} вызывает {@link TelegramIntegrationService#sendImage(String, byte[], String)}.
     * Для других типов ботов изображение сначала загружается на сервер (например, MAX) через
     * {@code maxIntegrationService.getUploadFileUrl(AttachmentType.IMAGE)} и
     * {@code maxIntegrationService.uploadImageAtServer(...)}. После получения токена формируется
     * сообщение с вложением изображения и отправляется с использованием экспоненциального
     * повторения при неудачах (количество попыток и параметры задержки задаются в конфигурации).
     *
     * @param chatId  идентификатор чата, куда отправляется изображение
     * @param botType тип бота, определяющий канал отправки
     * @param image   байтовый массив, представляющий изображение
     * @param caption подпись к изображению (может быть {@code null})
     * @throws IntegrationException если произошла ошибка при загрузке изображения или отправке сообщения
     * @throws RetryException        если исчерпаны все попытки отправки сообщения с вложением
     */
    void sendImage(String chatId, BotType botType, byte[] image, String caption);

    /**
     * Отправляет файл указанному чату через бота соответствующего типа.
     *
     * <p>Для {@link BotType#TELEGRAM} вызывает {@link TelegramIntegrationService#sendFile(String, String, byte[])}.
     * Для других типов ботов файл сначала загружается на сервер через
     * {@code maxIntegrationService.getUploadFileUrl(AttachmentType.FILE)} и
     * {@code maxIntegrationService.uploadFileAtServer(...)}. После получения токена формируется
     * сообщение с вложением файла и отправляется с экспоненциальным повторением при неудачах.
     *
     * @param chatId   идентификатор чата, куда отправляется файл
     * @param botType  тип бота, определяющий канал отправки
     * @param file     байтовый массив файла
     * @param fileName имя файла
     * @throws IntegrationException если произошла ошибка при загрузке файла или отправке сообщения
     * @throws RetryException        если исчерпаны все попытки отправки сообщения с вложением
     */
    void sendFile(String chatId, BotType botType, byte[] file, String fileName);

    /**
     * Отправляет текстовое сообщение одному чату через бота соответствующего типа.
     *
     * <p>Удобная обёртка над {@link #sendMessage(Set, BotType, String)}, которая передаёт
     * множество, состоящее из одного идентификатора чата.
     *
     * @param chatId  идентификатор чата
     * @param botType тип бота
     * @param message текст сообщения
     * @throws IntegrationException если произошла ошибка при отправке
     */
    default void sendMessage(String chatId, BotType botType, String message) {
        sendMessage(Set.of(chatId), botType, message);
    }

    /**
     * Отправляет текстовое сообщение набору чатов через бота соответствующего типа.
     *
     * <p>Для {@link BotType#TELEGRAM} вызывает {@link TelegramIntegrationService#sendMessage(Set, String)}
     * один раз для всего набора чатов.
     * Для других типов ботов формируется отдельный запрос {@code SendMessageRequest} для каждого
     * идентификатора из набора и отправляется через {@code maxIntegrationService.sendMessage(chatId, request)}
     * без повторных попыток.
     *
     * @param chatIds набор идентификаторов чатов, которым нужно отправить сообщение
     * @param botType тип бота, определяющий канал отправки
     * @param message текст сообщения
     * @throws IntegrationException если произошла ошибка при отправке сообщения
     */
    void sendMessage(Set<String> chatIds, BotType botType, String message);
}
