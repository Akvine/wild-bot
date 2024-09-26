package ru.akvine.wild.bot.exceptions.security;

public class OtpExpiredException extends RuntimeException {
    private final int otpCountLeft;

    public OtpExpiredException(int otpCountLeft) {
        this.otpCountLeft = otpCountLeft;
    }

    public int  getOtpCountLeft() {
        return otpCountLeft;
    }
}
