package ru.akvine.marketspace.bot.integration.base;

import org.junit.platform.commons.util.StringUtils;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

public class UpdateBuilder {
    private final Update update;
    private final Message message;
    private final Chat chat;
    private final User user;

    private String username;
    private String firstName;
    private String lastName;
    private String text;
    private Long chatId;

    public UpdateBuilder() {
        this.update = new Update();
        this.message = new Message();
        this.chat = new Chat();
        this.user = new User();

        message.setFrom(user);
        message.setChat(chat);

        update.setMessage(message);
    }

    public UpdateBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public UpdateBuilder withFirstname(String firstname) {
        this.firstName = firstname;
        return this;
    }

    public UpdateBuilder withLastname(String lastname) {
        this.lastName = lastname;
        return this;
    }

    public UpdateBuilder withText(String text) {
        this.text = text;
        return this;
    }

    public UpdateBuilder withChatId(String id) {
        return withChatId(Long.parseLong(id));
    }

    public UpdateBuilder withChatId(Long id) {
        this.chatId = id;
        return this;
    }

    public Update build() {
        if (StringUtils.isNotBlank(firstName)) {
            user.setFirstName(firstName);
        }

        if (StringUtils.isNotBlank(lastName)) {
            user.setLastName(lastName);
        }

        if (StringUtils.isNotBlank(username)) {
            user.setUserName(username);
        }

        if (StringUtils.isNotBlank(text)) {
            message.setText(text);
        }

        chat.setId(chatId);

        return update;
    }
}
