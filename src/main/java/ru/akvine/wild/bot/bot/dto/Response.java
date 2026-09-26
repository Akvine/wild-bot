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

    /** Обновление обработано ранее: отвечать не нужно */
    private boolean ignored;

    /**
     * Ответ «ничего не делать»: боты, получив его, ничего не отправляют пользователю
     */
    public static Response ignored(String chatId, BotType botType) {
        Response response = new Response(chatId, botType);
        response.setIgnored(true);
        return response;
    }

    public Response(String chatId, BotType botType) {
        this(chatId, null, botType);
    }

    public Response(String chatId, String text, BotType botType) {
        this.chatId = chatId;
        this.text = text;
        this.botType = botType;
    }
}
