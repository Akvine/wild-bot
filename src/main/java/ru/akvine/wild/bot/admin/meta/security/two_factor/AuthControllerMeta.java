package ru.akvine.wild.bot.admin.meta.security.two_factor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.akvine.wild.bot.admin.dto.common.Response;
import ru.akvine.wild.bot.admin.dto.security.auth.AuthCredentialsRequest;
import ru.akvine.wild.bot.admin.dto.security.auth.AuthFinishRequest;
import ru.akvine.wild.bot.admin.dto.security.auth.AuthNewOtpRequest;

@RequestMapping(value = "/security/two/factor/auth")
public interface AuthControllerMeta {
    @PostMapping(value = "/start")
    Response start(@Valid @RequestBody AuthCredentialsRequest request, HttpServletRequest httpServletRequest);

    @PostMapping(value = "/newotp")
    Response newotp(@Valid @RequestBody AuthNewOtpRequest request, HttpServletRequest httpServletRequest);

    @PostMapping(value = "/finish")
    Response finish(@Valid @RequestBody AuthFinishRequest request, HttpServletRequest httpServletRequest);

    @GetMapping(value = "/logout")
    Response logout(HttpServletRequest httpServletRequest);
}
