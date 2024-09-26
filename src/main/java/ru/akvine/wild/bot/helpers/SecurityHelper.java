package ru.akvine.wild.bot.helpers;

import com.google.common.base.Preconditions;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.config.security.SupportUserAuthentication;
import ru.akvine.wild.bot.exceptions.security.NoSessionException;
import ru.akvine.wild.bot.services.domain.admin.SupportUserModel;

@Component
@RequiredArgsConstructor
public class SecurityHelper {

    public void authenticate(SupportUserModel supportUser, HttpServletRequest request) {
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new SupportUserAuthentication(
                supportUser.getId(),
                supportUser.getUuid(),
                supportUser.getEmail()));

        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", context);
    }

    @Nullable
    public SupportUserAuthentication getCurrentUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof SupportUserAuthentication) {
            return (SupportUserAuthentication) authentication;
        }
        return null;
    }

    public void doLogout(HttpServletRequest request) {
        if (request == null) {
            return;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        SupportUserAuthentication user = getCurrentUserOrNull();
        if (user == null) {
            return;
        }

        session.removeAttribute("SPRING_SECURITY_CONTEXT");
        SecurityContextHolder.clearContext();
    }

    public SupportUserAuthentication getCurrentUser() {
        SupportUserAuthentication user = getCurrentUserOrNull();
        Preconditions.checkNotNull(user, "user is null");
        return user;
    }

    public HttpSession getSession(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            throw new NoSessionException();
        }
        return session;
    }
}
