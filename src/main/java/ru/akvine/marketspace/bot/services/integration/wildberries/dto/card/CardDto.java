package ru.akvine.marketspace.bot.services.integration.wildberries.dto.card;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
public class CardDto {
    private String nmID;
    private String title;
    private String subjectID;
    private String subjectName;
    private List<CardPhotoDto> photos;
    private List<SizeDto> sizes;
    private String updatedAt;
}
