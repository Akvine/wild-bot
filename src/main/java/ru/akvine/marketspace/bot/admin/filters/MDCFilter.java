package ru.akvine.marketspace.bot.admin.filters;


import jakarta.servlet.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import ru.akvine.marketspace.bot.constants.MDCConstants;

import java.io.IOException;

@Component
public class MDCFilter extends GenericFilter {
    private final static String ADMIN = "admin";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        MDC.put(MDCConstants.USERNAME, ADMIN);
        MDC.put(MDCConstants.CHAT_ID, ADMIN);
        filterChain.doFilter(servletRequest, servletResponse);
        MDC.clear();
    }
}
