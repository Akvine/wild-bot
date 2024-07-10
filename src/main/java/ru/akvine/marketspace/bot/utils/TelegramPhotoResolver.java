package ru.akvine.marketspace.bot.utils;

import com.google.common.base.Preconditions;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.util.List;

@Component
public class TelegramPhotoResolver {
    public PhotoSize resolve(List<PhotoSize> photos) {
        Preconditions.checkNotNull(photos, "photos is null");

        int photoSizeIndex = 0;
        int photoFullSize = 0;

        for (int i = 0; i < photos.size(); ++i) {
            if (photos.get(i).getFileSize() > photoFullSize) {
                photoSizeIndex = i;
            }
        }

        return photos.get(photoSizeIndex);
    }
}