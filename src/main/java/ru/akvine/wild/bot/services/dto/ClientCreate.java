package ru.akvine.wild.bot.services.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.enums.BotType;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class ClientCreate {
    private String chatId;
    private BotType botType;

    private String username;
    private String firstName;
    private String lastName;
}
