package ru.akvine.wild.bot.services.integration.max.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Sender {
    @JsonProperty(value = "user_id")
    private long userId;

    @JsonProperty(value = "first_name")
    private String firstName;

    @JsonProperty(value = "last_name")
    private String lastName;

    @JsonProperty(value = "is_bot")
    private boolean bot;

    @JsonProperty(value = "last_activity_time")
    private long lastActivityTime;

    @JsonProperty(value = "name")
    private String name;
}
