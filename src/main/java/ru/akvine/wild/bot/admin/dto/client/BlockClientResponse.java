package ru.akvine.wild.bot.admin.dto.client;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.common.SuccessfulResponse;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class BlockClientResponse extends SuccessfulResponse {
    private String uuid;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime;
    private long minutes;
}