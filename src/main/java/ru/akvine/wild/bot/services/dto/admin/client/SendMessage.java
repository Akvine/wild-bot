package ru.akvine.wild.bot.services.dto.admin.client;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@Accessors(chain = true)
public class SendMessage {
    @Nullable
    private List<String> chatIds;
    @Nullable
    private List<String> usernames;
    private String message;
}
