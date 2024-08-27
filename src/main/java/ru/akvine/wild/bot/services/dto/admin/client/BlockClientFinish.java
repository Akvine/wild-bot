package ru.akvine.wild.bot.services.dto.admin.client;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class BlockClientFinish {
    private String uuid;
    private LocalDateTime dateTime;
    private long minutes;
}
