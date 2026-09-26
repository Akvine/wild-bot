package ru.akvine.wild.bot.infrastructure.httplogging.checkers;

import jakarta.servlet.http.HttpServletRequest;

public class AllowAllMethodLoggingChecker implements MethodLoggingChecker {
    @Override
    public boolean shouldLogRequest(HttpServletRequest httpServletRequest) {
        return true;
    }

    @Override
    public boolean shouldLogResponse(HttpServletRequest httpServletRequest) {
        return true;
    }
}
