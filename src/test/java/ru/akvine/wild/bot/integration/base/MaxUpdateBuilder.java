package ru.akvine.wild.bot.integration.base;

import org.apache.commons.lang3.StringUtils;
import ru.akvine.wild.bot.enums.BotDataType;
import ru.akvine.wild.bot.services.integration.max.dto.*;

/**
 * Аналог {@link UpdateBuilder} для MAX: собирает {@link Update} и соответствующее ему
 * "входящее сообщение", которое тест должен подставить в мок {@code MaxIntegrationService
 * #getMessages(String)} — именно оттуда {@code MaxDtoConverter} берёт текст для MESSAGE-типа
 * обновлений (для CALLBACK текст берётся из {@code update.getCallback().getPayload()}).
 */
public class MaxUpdateBuilder {
    private final Update update = new Update();
    private final UpdateMessage updateMessage = new UpdateMessage();
    private final Sender sender = new Sender();
    private final Recipient recipient = new Recipient();

    private String chatId;
    private String firstName;
    private String lastName;
    private String text;

    public MaxUpdateBuilder() {
        updateMessage.setSender(sender);
        updateMessage.setRecipient(recipient);
        update.setUpdateMessage(updateMessage);
    }

    public MaxUpdateBuilder withChatId(String chatId) {
        this.chatId = chatId;
        return this;
    }

    public MaxUpdateBuilder withFirstname(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public MaxUpdateBuilder withLastname(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public MaxUpdateBuilder withText(String text) {
        this.text = text;
        return this;
    }

    public Update build() {
        return build(BotDataType.MESSAGE);
    }

    public Update build(BotDataType type) {
        recipient.setChatId(chatId);
        if (StringUtils.isNotBlank(firstName)) {
            sender.setFirstName(firstName);
        }
        if (StringUtils.isNotBlank(lastName)) {
            sender.setLastName(lastName);
        }

        if (type == BotDataType.CALLBACK) {
            update.setUpdateType("message_callback");
            update.setCallback(new Callback().setPayload(text));
        } else {
            update.setUpdateType("message_created");
            update.setCallback(null);
        }

        return update;
    }

    /**
     * Сообщение, которое должен возвращать замоканный {@code MaxIntegrationService
     * #getMessages(chatId)} для текста, заданного через {@link #withText}. Нужно только для
     * MESSAGE-обновлений (для CALLBACK тело сообщения конвертером не читается, но
     * {@code MaxDummyBot}/{@code MaxDevBot} всё равно требуют непустой массив сообщений).
     */
    public Message buildIncomingMessage() {
        return new Message().setBody(new Body().setText(text));
    }
}
