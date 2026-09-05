package ru.akvine.wild.bot.integration.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.akvine.wild.bot.constants.telegram.BotMessageErrorConstants.CLIENT_HAS_BLOCKED_MESSAGE_PREFIX;
import static ru.akvine.wild.bot.constants.telegram.BotMessageErrorConstants.CLIENT_NOT_IN_WHITELIST_MESSAGE;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.wild.bot.integration.base.TelegramBaseTest;
import ru.akvine.wild.bot.integration.base.UpdateBuilder;

@ExtendWith(SpringExtension.class)
@DisplayName("Filters tests")
public class FiltersTest extends TelegramBaseTest {

    @Test
    @DisplayName("New client not in whitelist")
    void new_client_not_in_whitelist() {
        builder = new UpdateBuilder();
        builder.withFirstname("FirstName");
        builder.withLastname("SecondName");
        builder.withText("some text");
        builder.withUsername("some username");
        builder.withChatId("-1");

        Update update = builder.build();
        BotApiMethod<?> apiMethod = telegramBot.onWebhookUpdateReceived(update);
        SendMessage message = (SendMessage) apiMethod;

        String text = message.getText();

        assertThat(text).isNotNull();
        assertThat(text).isEqualTo(CLIENT_NOT_IN_WHITELIST_MESSAGE);
    }

    @Test
    @DisplayName("Client is blocked")
    void client_is_blocked() {
        builder = new UpdateBuilder();
        builder.withFirstname("FirstName");
        builder.withLastname("SecondName");
        builder.withText("some text");
        builder.withUsername("some username");
        builder.withChatId("1");

        Update update = builder.build();
        BotApiMethod<?> apiMethod = telegramBot.onWebhookUpdateReceived(update);
        ;
        SendMessage message = (SendMessage) apiMethod;

        String text = message.getText();

        assertThat(text).isNotNull();
        assertThat(text.contains(CLIENT_HAS_BLOCKED_MESSAGE_PREFIX)).isTrue();
    }
}
