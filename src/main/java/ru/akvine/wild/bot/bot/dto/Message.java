package ru.akvine.wild.bot.bot.dto;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

@Data
@Accessors(chain = true)
public class Message {
    private String text;
    private List<PhotoSize> photo;

    public boolean hasText() {
        return text != null && !text.trim().isEmpty();
    }

    // TODO: надо реализовать
    public boolean hasSticker() {
        return false;
    }
}
