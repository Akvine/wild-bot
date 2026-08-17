package ru.akvine.wild.bot.bot.dto;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

@Data
@Accessors(chain = true)
public class Message {
    private String text;
    private List<PhotoSize> telegramPhoto;

    /**
     * Прямая ссылка на фото-вложение сообщения MAX (см. {@code ReceivedAttachment}) — в
     * отличие от Telegram, где фото передаётся набором {@code file_id} для скачивания через
     * отдельный API-вызов, MAX сразу отдаёт готовый URL.
     */
    private String maxPhotoUrl;

    public boolean hasText() {
        return text != null && !text.trim().isEmpty();
    }

    // TODO: надо реализовать
    public boolean hasSticker() {
        return false;
    }
}
