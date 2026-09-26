package ru.akvine.wild.bot.infrastructure.httplogging.checkers;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Решает, нужно ли логировать запрос и ответ
 */
public interface MethodLoggingChecker {
    boolean shouldLogRequest(HttpServletRequest httpServletRequest);

    boolean shouldLogResponse(HttpServletRequest httpServletRequest);
}
