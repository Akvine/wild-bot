package ru.akvine.wild.bot.services.integration.max.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class User {
    @JsonProperty(value = "user_id")
    private String userId;

    @JsonProperty(value = "first_name")
    private String firstName;

    @JsonProperty(value = "last_name")
    private String lastName;

    @JsonProperty(value = "username")
    private String username;

    @JsonProperty(value = "is_bot")
    private boolean bot;
}
