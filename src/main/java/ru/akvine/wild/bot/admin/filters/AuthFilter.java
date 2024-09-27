package ru.akvine.wild.bot.admin.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.exceptions.UrlPathException;

import java.io.IOException;

@Component
public class AuthFilter extends GenericFilter {
    @Value("${security.two.factor.authentication.enabled}")
    private Boolean twoFactorAuthEnabled;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        String url = httpRequest.getRequestURL().toString();
        validate(url);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void validate(String url) {
        if (StringUtils.isNotBlank(url) && url.contains("/two/factor") && !twoFactorAuthEnabled) {
            throw new UrlPathException("Two factor authentication is not supported now by app");
        }
        if (StringUtils.isNotBlank(url) && url.contains("/one/factor") && twoFactorAuthEnabled) {
            throw new UrlPathException("Required using two factor authentication methods");
        }
    }
}
