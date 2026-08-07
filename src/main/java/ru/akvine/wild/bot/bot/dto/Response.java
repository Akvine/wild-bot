package ru.akvine.wild.bot.bot.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.services.integration.max.dto.MaxSendMessage;

@Getter
@Accessors(chain = true)
@Setter
@NoArgsConstructor
public final class Response {
    private String chatId;
    private String text;
    private BotType botType;

    private SendMessage telegramResponse;
    private MaxSendMessage maxSendMessage;

    public Response(String chatId, BotType botType) {
        this(chatId, null, botType);
    }

    public Response(String chatId, String text, BotType botType) {
        this.chatId = chatId;
        this.text = text;
        this.botType = botType;
    }
}
