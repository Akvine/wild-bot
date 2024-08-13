package ru.akvine.marketspace.bot.flow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.flow.base.BaseTest;
import ru.akvine.marketspace.bot.flow.base.UpdateBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.akvine.marketspace.bot.config.TestConstants.CHAT_ID_2;
import static ru.akvine.marketspace.bot.constants.TelegramMessageErrorConstants.CLIENT_SUBSCRIPTION_MESSAGE;
import static ru.akvine.marketspace.bot.constants.TelegramMessageErrorConstants.UNKNOWN_COMMAND_MESSAGE;

@ExtendWith(SpringExtension.class)
@DisplayName("Bot flow tests")
public class BotFlowTest extends BaseTest {

    @Test
    @DisplayName("User not in whitelist")
    public void user_not_in_whitelist() {
        builder = new UpdateBuilder();
        builder.withFirstname("FirstName");
        builder.withLastname("SecondName");
        builder.withText("some text");
        builder.withUsername("some username");
        builder.withChatId("1");

        Update update = builder.build();
        BotApiMethod<?> apiMethod = messageFilter.handle(update);
        SendMessage message = (SendMessage) apiMethod;

        String text = message.getText();
        String chatId = message.getChatId();

        assertThat(text).isNotNull();
        assertThat(text).isEqualTo(CLIENT_SUBSCRIPTION_MESSAGE);
        assertThat(chatId).isNotNull();
        assertThat(chatId).isEqualTo("1");
    }

    @Test
    @DisplayName("User in whitelist")
    public void user_in_whitelist() {
        builder = new UpdateBuilder();
        builder.withChatId(CHAT_ID_2);

        Update update = builder.build();
        BotApiMethod<?> apiMethod = messageFilter.handle(update);
        SendMessage message = (SendMessage) apiMethod;

        String text = message.getText();
        String chatId = message.getChatId();

        assertThat(text).isNotNull();
        assertThat(text).isEqualTo(UNKNOWN_COMMAND_MESSAGE);
        assertThat(chatId).isNotNull();
        assertThat(chatId).isEqualTo(CHAT_ID_2);
    }
}
