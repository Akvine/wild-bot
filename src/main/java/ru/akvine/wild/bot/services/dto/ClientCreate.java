package ru.akvine.marketspace.bot.services.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class ClientCreate {
    private String chatId;
    private String username;
    private String firstName;
    private String lastName;
}
