package ru.akvine.wild.bot.max;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.BACK_BUTTON_TEXT;

import lombok.experimental.UtilityClass;
import ru.akvine.wild.bot.services.integration.max.dto.Attachment;
import ru.akvine.wild.bot.services.integration.max.dto.AttachmentType;
import ru.akvine.wild.bot.services.integration.max.dto.Button;
import ru.akvine.wild.bot.services.integration.max.dto.Payload;

/**
 * Аналог {@link ru.akvine.wild.bot.telegram.TelegramKeyboardFactory} для MAX: собирает
 * inline-кнопки в формате MAX Bot API (см. POST /messages, вложение {@code inline_keyboard}).
 * Используются только кнопки типа {@code callback} — по нажатию MAX отправляет боту событие
 * {@code message_callback} с тем же текстом, что и в {@link Button#getPayload()}, аналогично
 * {@code callback_data} у Telegram-кнопок.
 */
@UtilityClass
public class MaxComponentsFactory {
    private static final String CALLBACK_BUTTON_TYPE = "callback";
    private static final String INLINE_KEYBOARD_ATTACHMENT_TYPE = "inline_keyboard";

    public Button callbackButton(String text) {
        return new Button().setType(CALLBACK_BUTTON_TYPE).setText(text).setPayload(text);
    }

    public Button getBackButton() {
        return callbackButton(BACK_BUTTON_TEXT);
    }

    /**
     * Клавиатура из одной кнопки "Назад" — аналог
     * {@link ru.akvine.wild.bot.telegram.TelegramKeyboardFactory#getBackKeyboard}.
     */
    public Button[][] getBackKeyboard() {
        return new Button[][] {{getBackButton()}};
    }

    /**
     * Кладёт каждую кнопку в отдельный ряд — аналог
     * {@link ru.akvine.wild.bot.telegram.TelegramKeyboardFactory#createVerticalKeyboard}.
     */
    public Button[][] createVerticalKeyboard(Button... buttons) {
        Button[][] keyboard = new Button[buttons.length][];
        for (int i = 0; i < buttons.length; i++) {
            keyboard[i] = new Button[] {buttons[i]};
        }
        return keyboard;
    }

    /**
     * Оборачивает ряды кнопок во вложение {@code inline_keyboard}, готовое положить
     * в {@code MaxSendMessage#getAttachments()} (см. MAX Bot API: POST /messages).
     */
    public Attachment toInlineKeyboardAttachment(Button[][] buttons) {
        return new Attachment().setType(INLINE_KEYBOARD_ATTACHMENT_TYPE).setPayload(new Payload().setButtons(buttons));
    }

    /**
     * Создает attachment при отправке файла
     * @param token токен для отправки файла
     * @return {@link Attachment}
     */
    public Attachment[] createFileAttachment(AttachmentType type, String token) {
        Attachment attachment =
                new Attachment().setType(type.toString().toLowerCase()).setPayload(new Payload().setToken(token));

        return new Attachment[] {attachment};
    }
}
