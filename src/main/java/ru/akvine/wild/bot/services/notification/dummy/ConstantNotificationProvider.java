package ru.akvine.wild.bot.services.notification.dummy;

public class ConstantNotificationProvider implements DummyNotificationProvider {
    @Override
    public boolean sendRegistrationCode(String login, String code) {
        return true;
    }

    @Override
    public boolean sendAuthenticationCode(String login, String code) {
        return true;
    }

    @Override
    public boolean sendAccessRestoreCode(String login, String code) {
        return true;
    }

    @Override
    public boolean sendProfileDeleteCode(String login, String code) {
        return true;
    }

    @Override
    public boolean sendProfileChangeEmailCode(String login, String code) {
        return true;
    }

    @Override
    public boolean sendProfileChangePasswordCode(String login, String code) {
        return true;
    }
}
