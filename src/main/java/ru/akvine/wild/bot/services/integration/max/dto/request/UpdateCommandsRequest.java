package ru.akvine.wild.bot.services.integration.max.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.services.integration.max.dto.CommandDto;

@Data
@Accessors(chain = true)
public class UpdateCommandsRequest {
    @JsonProperty(value = "commands")
    private CommandDto[] commands;
}
