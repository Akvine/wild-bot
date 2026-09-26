package ru.akvine.wild.bot.infrastructure.httplogging.checkers;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.util.AntPathMatcher;

/**
 * Не логирует запросы к actuator
 */
public class ActuatorLoggingChecker implements MethodLoggingChecker {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final String[] actuatorPatterns = new String[] {"/actuator/**/*", "/actuator"};

    @Override
    public boolean shouldLogRequest(HttpServletRequest request) {
        if (request == null) {
            return true;
        }
        String path = request.getPathInfo() != null ? request.getPathInfo() : request.getServletPath();
        return Arrays.stream(actuatorPatterns).noneMatch(pattern -> antPathMatcher.match(pattern, path));
    }

    @Override
    public boolean shouldLogResponse(HttpServletRequest httpServletRequest) {
        return shouldLogRequest(httpServletRequest);
    }
}
