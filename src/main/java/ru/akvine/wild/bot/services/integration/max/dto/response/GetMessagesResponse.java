package ru.akvine.wild.bot.services.integration.max.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.services.integration.max.dto.Message;

@Data
@Accessors(chain = true)
public class GetMessagesResponse {
    @JsonProperty(value = "messages")
    private Message[] messages;
}
