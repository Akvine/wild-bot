package ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AdvertUploadPhotoRequest {
    private int photoNumber;
    private int nmId;
    @ToString.Exclude
    private byte[] uploadFile;
}
