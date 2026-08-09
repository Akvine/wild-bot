package ru.akvine.wild.bot.services.dto.admin.client;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BlockClientEntry {
    private String chatId;
    private long minutes;
    private LocalDateTime blockStartDate;
    private LocalDateTime blockEndDate;
}
