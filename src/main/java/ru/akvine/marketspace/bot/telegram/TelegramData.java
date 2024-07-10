package ru.akvine.marketspace.bot.telegram;

import lombok.Data;
import lombok.experimental.Accessors;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.enums.TelegramDataType;

@Data
@Accessors(chain = true)
public class TelegramData {
    private Update data;
    private TelegramDataType type;
}
