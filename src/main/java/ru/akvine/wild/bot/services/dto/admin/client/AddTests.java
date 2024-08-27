package ru.akvine.marketspace.bot.services.dto.admin.client;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Data
@Accessors(chain = true)
public class AddTests {
    @Nullable
    private String chatId;
    @Nullable
    private String username;
    private int testsCount;
}
