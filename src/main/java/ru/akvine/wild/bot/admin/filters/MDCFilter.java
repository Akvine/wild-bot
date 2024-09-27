package ru.akvine.wild.bot.admin.filters;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import ru.akvine.wild.bot.constants.MDCConstants;

import java.io.IOException;

@Component
public class MDCFilter extends GenericFilterBean {
    private final static String ADMIN = "admin";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        MDC.put(MDCConstants.USERNAME, ADMIN);
        MDC.put(MDCConstants.CHAT_ID, ADMIN);
        filterChain.doFilter(servletRequest, servletResponse);
        MDC.clear();
    }
}
