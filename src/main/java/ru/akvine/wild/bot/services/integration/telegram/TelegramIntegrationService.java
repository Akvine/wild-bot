package ru.akvine.wild.bot.services.integration.telegram;

import ru.akvine.wild.bot.telegram.TelegramData;

import java.io.InputStream;
import java.util.List;

public interface TelegramIntegrationService {

    /**
     * Отправить answerCallback
     *
     * @param telegramData данные, отправленные пользователем
     */
    void answerCallback(TelegramData telegramData);

    /**
     * Загрузить фото, которое отправил пользователь с сервера телеграмма через API
     *
     * @param photoId id фотографии, полученный из Message пользователя
     * @param chatId  id чата пользователя
     * @return фотографию
     */
    byte[] downloadPhoto(String photoId, String chatId);


    /**
     * Отправка файла пользователю
     *
     * @param chatId   id чата куда будет отправлен файл
     * @param fileName имя файла вместе с расширением
     */
    void sendFile(String chatId, String fileName, byte[] file);

    /**
     * Отправка файла пользователю
     *
     * @param chatId   id чата куда будет отправлен файл
     * @param fileName имя файла вместе с расширением
     */
    void sendFile(String chatId, String fileName, InputStream file);

    /**
     * Отправка сообщения конкретному пользователю
     *
     * @param chatId  идентификатор чата пользователя
     * @param message сообщение
     */
    default void sendMessage(String chatId, String message) {
        sendMessage(List.of(chatId), message);
    }

    /**
     * Отправка сообщения пользователям
     *
     * @param chatIds идентификаторы чатов пользователей
     * @param message сообщение
     */
    void sendMessage(List<String> chatIds, String message);

    /**
     * Отправка изображения пользователю
     *
     * @param chatId идентификатор пользователя
     * @param image  изображение
     */
    void sendImage(String chatId, byte[] image);

    /**
     * Отправка изображения пользователю
     *
     * @param chatId идентификатор пользователя
     * @param image  изображение
     */
    void sendImage(String chatId, InputStream image);

    /**
     * Отправка изображения пользователю
     *
     * @param chatId  идентификатор пользователя
     * @param image   изображение
     * @param caption Подпись к фотографии
     */
    void sendImage(String chatId, byte[] image, String caption);

    /**
     * Отправка изображения пользователю
     *
     * @param chatId  идентификатор пользователя
     * @param image   изображение
     * @param caption Подпись к фотографии
     */
    void sendImage(String chatId, InputStream image, String caption);
}
