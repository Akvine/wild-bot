package ru.akvine.marketspace.bot.telegram.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.enums.TelegramDataType;
import ru.akvine.marketspace.bot.telegram.TelegramData;
import ru.akvine.marketspace.bot.telegram.dispatcher.MessageDispatcher;

@RequiredArgsConstructor
@Slf4j
public class UpdateConverterFilter extends MessageFilter {
    private final MessageDispatcher messageDispatcher;

    @Override
    public BotApiMethod<?> handle(Update update) {
        TelegramData telegramUpdateData = new TelegramData();
        telegramUpdateData.setData(update);
        if (update.getCallbackQuery() != null) {
            telegramUpdateData.setType(TelegramDataType.CALLBACK);
        } else {
            telegramUpdateData.setType(TelegramDataType.MESSAGE);
        }
        return messageDispatcher.doDispatch(telegramUpdateData);
    }
}
