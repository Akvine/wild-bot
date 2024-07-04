package ru.akvine.marketspace.bot.utils;

import com.google.common.base.Preconditions;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.util.List;

@Component
public class TelegramPhotoResolver {
    public PhotoSize resolve(Message message) {
        Preconditions.checkNotNull(message, "message is null");

        List<PhotoSize> photoSizes = message.getPhoto();
        int photoSizeIndex = 0;
        int photoFullSize = 0;

        for (int i = 0; i < photoSizes.size(); ++i) {
            if (photoSizes.get(i).getFileSize() > photoFullSize) {
                photoSizeIndex = i;
            }
        }

        return photoSizes.get(photoSizeIndex);
    }
}