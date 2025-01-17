package ru.akvine.wild.bot.telegram.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.enums.TelegramDataType;
import ru.akvine.wild.bot.telegram.TelegramData;
import ru.akvine.wild.bot.telegram.dispatcher.MessageDispatcher;
import ru.akvine.wild.bot.utils.TelegramUtils;

@RequiredArgsConstructor
@Slf4j
public class UpdateConverterFilter extends MessageFilter {
    private final MessageDispatcher messageDispatcher;

    @Override
    public BotApiMethod<?> handle(Update update) {
        String chatId = getChatId(update);
        logger.debug("Update data was reached in UpdateConverterFilter filter for chat with id = {}", chatId);
        TelegramData telegramUpdateData = new TelegramData();
        telegramUpdateData.setData(update);
        if (update.hasCallbackQuery()) {
            logger.debug("Message type for chat id = {} is [CALLBACK]", chatId);
            telegramUpdateData.setType(TelegramDataType.CALLBACK);
        } else {
            logger.debug("Message type for chat id = {} is [MESSAGE]", chatId);
            telegramUpdateData.setType(TelegramDataType.MESSAGE);
            if (TelegramUtils.isOnlySticker(telegramUpdateData)) {
                return new SendMessage(chatId, "Необходимо выбрать из меню или ввести команду!");
            }
        }

        return messageDispatcher.doDispatch(telegramUpdateData);
    }
}
