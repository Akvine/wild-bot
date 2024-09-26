package ru.akvine.wild.bot.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import ru.akvine.wild.bot.admin.dto.common.SuccessfulResponse;

import java.io.IOException;

public class RestSuccessLogoutHandler implements LogoutSuccessHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final SuccessfulResponse LOGOUT_SUCCESS_RESPONSE = new SuccessfulResponse();

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getOutputStream().println(objectMapper.writeValueAsString(LOGOUT_SUCCESS_RESPONSE));
    }
}
