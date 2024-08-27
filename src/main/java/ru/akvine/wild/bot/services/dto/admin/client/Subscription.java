package ru.akvine.wild.bot.services.dto.admin.client;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Data
@Accessors(chain = true)
public class Subscription {
    @Nullable
    private String chatId;
    @Nullable
    private String username;
}
