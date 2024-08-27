package ru.akvine.wild.bot.services.dto.admin.client;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class BlockClientEntry {
    private String uuid;
    private long minutes;
    private LocalDateTime blockStartDate;
    private LocalDateTime blockEndDate;
}
