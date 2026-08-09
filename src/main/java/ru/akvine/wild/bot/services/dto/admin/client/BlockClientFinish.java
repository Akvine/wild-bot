package ru.akvine.wild.bot.services.dto.admin.client;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BlockClientFinish {
    private String chatId;
    private LocalDateTime dateTime;
    private long minutes;
}
