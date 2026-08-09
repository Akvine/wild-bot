package ru.akvine.wild.bot.bot.converter;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.akvine.wild.bot.bot.dto.Message;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotDataType;
import ru.akvine.wild.bot.enums.BotType;

@Component
public class TelegramDtoConverter implements BotDtoConverter<Update, BotApiMethod<?>> {
    @Override
    public Payload fromRequest(Update update) {
        Payload payload = new Payload().setBotType(getType()).setChatId(getChatId(update));

        if (update.getMessage() != null && update.getMessage().getFrom() != null) {
            User user = update.getMessage().getFrom();

            payload.setFirstName(user.getFirstName());
            payload.setLastName(user.getLastName());
            payload.setUsername(user.getUserName());
        }

        payload.setMessage(new Message());
        if (update.hasCallbackQuery()) {
            payload.setBotDataType(BotDataType.CALLBACK);
            payload.setTelegramCallbackQueryId(update.getCallbackQuery().getId());
            payload.getMessage().setText(update.getCallbackQuery().getData());
        } else {
            payload.getMessage().setText(update.getMessage().getText());
            payload.setBotDataType(BotDataType.MESSAGE);
        }

        return payload;
    }

    @Override
    public BotApiMethod<?> toResponse(Response response) {
        if (response.getTelegramResponse() == null) {
            return new SendMessage(response.getChatId(), response.getText());
        }

        return response.getTelegramResponse();
    }

    @Override
    public BotType getType() {
        return BotType.TELEGRAM;
    }

    private String getChatId(Update update) {
        if (update.getCallbackQuery() != null) {
            return String.valueOf(update.getCallbackQuery().getMessage().getChatId());
        } else {
            return String.valueOf(update.getMessage().getChatId());
        }
    }
}
