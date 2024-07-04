package ru.akvine.marketspace.bot.services.integration.telegram;

import java.io.InputStream;
import java.util.List;

public interface TelegramIntegrationService {
    /**
     * Загрузить фото, которое отправил пользователь с сервера телеграмма через API
     * @param photoId id фотографии, полученный из Message пользователя
     * @param chatId id чата пользователя
     * @return фотографию
     */
    byte[] downloadPhoto(String photoId, String chatId);

    /**
     * Отправка файла пользователю
     * @param chatId id чата куда будет отправлен файл
     * @param fileName имя файла вместе с расширением
     */
    void sendFile(InputStream file, String fileName, String chatId);

    /**
     * Отправка сообщения пользователям
     * @param chatIds идентификаторы чатов пользователей
     * @param message сообщение
     */
    void sendMessage(List<String> chatIds, String message);
}
