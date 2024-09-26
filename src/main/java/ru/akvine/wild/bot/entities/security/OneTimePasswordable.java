package ru.akvine.wild.bot.entities.security;

public interface OneTimePasswordable {
    Long getId();
    String getLogin();
    OtpActionEntity getOtpAction();
    String getSessionId();
}
