package ru.akvine.wild.bot.services.integration.max.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Payload вложения типа {@code image} во входящем сообщении (см.
 * <a href="https://dev.max.ru/docs-api/methods/GET/videos/-videoToken-">dev.max.ru</a>,
 * где этот же объект возвращается как {@code thumbnail} видео). {@code url} — прямая ссылка,
 * по которой можно скачать файл без дополнительных вызовов MAX API.
 */
@Data
@Accessors(chain = true)
public class PhotoAttachmentPayload {
    @JsonProperty("photo_id")
    private long photoId;

    private String token;

    private String url;
}
