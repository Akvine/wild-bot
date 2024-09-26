package ru.akvine.wild.bot.entities.security;

public interface AccountPasswordable {
    int decrementPwdInvalidAttemptsLeft();
    int getPwdInvalidAttemptsLeft();
}
