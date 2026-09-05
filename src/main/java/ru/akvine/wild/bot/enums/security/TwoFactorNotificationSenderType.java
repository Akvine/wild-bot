package ru.akvine.wild.bot.enums.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.services.notification.TwoFactorNotificationSender;
import ru.akvine.wild.bot.services.notification.dummy.ConstantTwoFactorNotificationSender;
import ru.akvine.wild.bot.services.notification.dummy.LogTwoFactorNotificationSender;

@AllArgsConstructor
@Getter
public enum TwoFactorNotificationSenderType {
    LOG("log", new LogTwoFactorNotificationSender()),
    CONSTANT("constant", new ConstantTwoFactorNotificationSender());

    private final String code;
    private final TwoFactorNotificationSender senderObject;

    public static TwoFactorNotificationSender resolveByCodeAndBotType(String code) {
        for (TwoFactorNotificationSenderType senderType : values()) {
            if (senderType.getCode().equalsIgnoreCase(code)) {
                return senderType.getSenderObject();
            }
        }

        String errorMessage = String.format("Unsupported notification provider type = [%s]", code);
        throw new IllegalArgumentException(errorMessage);
    }
}
