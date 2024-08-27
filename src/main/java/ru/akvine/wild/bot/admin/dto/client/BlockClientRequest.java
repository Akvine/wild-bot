package ru.akvine.wild.bot.admin.dto.client;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.admin.dto.common.SecretRequest;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class BlockClientRequest extends BlockRequest {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;
}
