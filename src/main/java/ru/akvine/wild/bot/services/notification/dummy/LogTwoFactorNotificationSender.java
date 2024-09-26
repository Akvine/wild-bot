package ru.akvine.wild.bot.services.notification.dummy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogTwoFactorNotificationSender implements DummyTwoFactorNotificationSender {
    @Override
    public boolean sendRegistrationCode(String login, String code) {
        logger.info("Successful send registration code = [{}] to login = [{}]", code, login);
        return true;
    }

    @Override
    public boolean sendAuthenticationCode(String login, String code) {
        logger.info("Successful send authentication code = [{}] to login = [{}]", code, login);
        return true;
    }

    @Override
    public boolean sendAccessRestoreCode(String login, String code) {
        logger.info("Successful send access restore code = [{}}] to login = [{}]", code, login);
        return true;
    }

    @Override
    public boolean sendProfileDeleteCode(String login, String code) {
        logger.info("Successful send delete profile code = [{}] to login = [{}]", code, login);
        return true;
    }

    @Override
    public boolean sendProfileChangeEmailCode(String login, String code) {
        logger.info("Successful send profile change email code = [{}] to login = [{}]", code, login);
        return true;
    }

    @Override
    public boolean sendProfileChangePasswordCode(String login, String code) {
        logger.info("Successful send profile change password code = [{}] to login = [{}]", code, login);
        return true;
    }
}
