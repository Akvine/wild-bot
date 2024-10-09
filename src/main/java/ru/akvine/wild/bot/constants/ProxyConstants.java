package ru.akvine.wild.bot.constants;

public final class ProxyConstants {

    private ProxyConstants() throws IllegalAccessException {
        throw new IllegalAccessException("Calling " + ProxyConstants.class.getSimpleName() + " constructor is prohibited");
    }

    public final static String ORIGIN_BEAN_NAME = "origin";
}
