package ru.akvine.wild.bot.services.integration.max;

import ru.akvine.wild.bot.services.integration.max.dto.AttachmentType;
import ru.akvine.wild.bot.services.integration.max.dto.Message;
import ru.akvine.wild.bot.services.integration.max.dto.Update;
import ru.akvine.wild.bot.services.integration.max.dto.request.SendMessageRequest;
import ru.akvine.wild.bot.services.integration.max.dto.response.LongPoolingSubscriptionResponse;

/**
 * Низкоуровневый клиент реального MAX Bot API ({@code platform-api2.max.ru}) — три метода,
 * напрямую соответствующие вызовам {@code GET /updates}, {@code GET /messages} и
 * {@code POST /messages} (см. <a href="https://dev.max.ru/docs-api">dev.max.ru/docs-api</a>).
 * Сервис ничего не знает о внутренней модели бота ({@code Payload}/{@code Response}) — той
 * стороной занимается {@code MaxDtoConverter}, который использует этот интерфейс как
 * транспорт. Единственная реализация — {@code MaxIntegrationServiceOrigin}.
 */
public interface MaxIntegrationService {
    /**
     * Метод получения обновления о событиях через Long Pooling
     *
     * Используется только в test / dev окружении
     * @return {@link LongPoolingSubscriptionResponse}
     */
    Update[] updates();

    /**
     * Метод получения сообщений из chatId
     *
     * @param chatId уникальный идентификатор чата в боте
     * @return список сообщений
     */
    Message[] getMessages(String chatId);

    /**
     * Метод по отправке сообщений
     */
    void sendMessage(String chatId, SendMessageRequest request);

    /**
     * Скачивает файл вложения (например, фото карточки) по прямой ссылке, полученной в
     * {@code payload.url} вложения входящего сообщения — в отличие от Telegram, отдельного
     * API-метода "получить файл по токену" для этого не требуется.
     *
     * @param fileUrl прямая ссылка на файл
     * @return содержимое файла
     */
    byte[] downloadAttachment(String fileUrl);

    /**
     * Метод для получения url для прикрепления файла во вложение
     * @param type тип вложения (image, file, video, audio и т.д.)
     * @return URL для прикрепления файла
     */
    String getUploadFileUrl(AttachmentType type);

    /**
     * Загружает файл на файловый хостинг-сервис MAX
     * @param url ссылка, по которой необходимо загрузить файл (получается на шаге getUploadFileUrl)
     * @param file файл в байтовом представлении
     * @return токен
     */
    String uploadFileAtServer(String url, byte[] file, String fileName);

    /**
     * Загружает изображение на файловый хостинг-сервис MAX
     *
     * @param url ссылка, по которой необходимо загрузить файл (получается на шаге getUploadFileUrl)
     * @param image изображение в байтовом представлении
     * @param imageName название изображения
     * @return токен
     */
    String uploadImageAtServer(String url, byte[] image, String imageName);
}
