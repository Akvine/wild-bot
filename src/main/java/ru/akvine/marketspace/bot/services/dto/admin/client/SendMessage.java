package ru.akvine.marketspace.bot.services.dto.admin.client;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SendMessage {
    private List<String> chatIds;
    private String message;
}
