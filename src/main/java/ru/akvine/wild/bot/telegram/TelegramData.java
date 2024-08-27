package ru.akvine.wild.bot.telegram;

import lombok.Data;
import lombok.experimental.Accessors;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.enums.TelegramDataType;

@Data
@Accessors(chain = true)
public class TelegramData {
    private Update data;
    private TelegramDataType type;
}
