package ru.akvine.wild.bot.infrastructure.httplogging.checkers;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Логирует, только если все вложенные проверки разрешили
 */
@Slf4j
public class CompositeMethodLoggingChecker implements MethodLoggingChecker {

    private final List<MethodLoggingChecker> methodLoggingCheckers;

    public CompositeMethodLoggingChecker(List<MethodLoggingChecker> methodLoggingCheckers) {
        this.methodLoggingCheckers = methodLoggingCheckers;
    }

    @Override
    public boolean shouldLogRequest(HttpServletRequest httpServletRequest) {
        boolean shouldLog = methodLoggingCheckers.stream()
                .filter(checker -> !(checker instanceof CompositeMethodLoggingChecker)) // alien protection
                .allMatch(methodLoggingChecker -> methodLoggingChecker.shouldLogRequest(httpServletRequest));

        if (!shouldLog) {
            logger.trace("the request logging is ignored by checkers");
        }

        return shouldLog;
    }

    @Override
    public boolean shouldLogResponse(HttpServletRequest httpServletRequest) {
        boolean shouldLog = methodLoggingCheckers.stream()
                .filter(checker -> !(checker instanceof CompositeMethodLoggingChecker)) // alien protection
                .allMatch(methodLoggingChecker -> methodLoggingChecker.shouldLogResponse(httpServletRequest));

        if (!shouldLog) {
            logger.trace("the response logging is ignored by checkers");
        }

        return shouldLog;
    }
}
