package ru.akvine.wild.bot.services.integration.max.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Вложение входящего сообщения ({@code Body.attachments}) — не путать с {@link Attachment},
 * который используется для исходящей inline-клавиатуры. У MAX вложения разных типов
 * (image/video/audio/file/contact/...) имеют разную форму {@code payload}; сейчас нас
 * интересуют только фото, поэтому {@code payload} типизирован как {@link PhotoAttachmentPayload}
 * — для остальных типов вложений поля payload останутся пустыми, что безопасно, так как
 * они и не читаются.
 */
@Data
@Accessors(chain = true)
public class ReceivedAttachment {
    private String type;

    private PhotoAttachmentPayload payload;
}
