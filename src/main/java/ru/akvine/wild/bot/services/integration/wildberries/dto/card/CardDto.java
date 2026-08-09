package ru.akvine.wild.bot.services.integration.wildberries.dto.card;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CardDto {
    private int nmID;
    private String title;
    private int subjectID;
    private String subjectName;
    private List<CardPhotoDto> photos;
    private List<SizeDto> sizes;
    private String updatedAt;
    private List<CardCharacteristic> characteristics;
}
