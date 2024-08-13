package ru.akvine.marketspace.bot.admin.filters;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.akvine.marketspace.bot.constants.MDCConstants;

import java.io.IOException;

@Component
public class MDCFilter extends OncePerRequestFilter {
    private final static String ADMIN = "admin";

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        MDC.put(MDCConstants.USERNAME, ADMIN);
        MDC.put(MDCConstants.CHAT_ID, ADMIN);
        filterChain.doFilter(request, response);
        MDC.clear();
    }
}
