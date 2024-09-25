package ru.akvine.wild.bot.services.notification;

public interface NotificationProvider {
    boolean sendRegistrationCode(String login, String code);
    boolean sendAuthenticationCode(String login, String code);
    boolean sendAccessRestoreCode(String login, String code);
    boolean sendProfileDeleteCode(String login, String code);
    boolean sendProfileChangeEmailCode(String login, String code);
    boolean sendProfileChangePasswordCode(String login, String code);
}
