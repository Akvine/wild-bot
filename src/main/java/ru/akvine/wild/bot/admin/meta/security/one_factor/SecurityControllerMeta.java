package ru.akvine.wild.bot.admin.meta.security.one_factor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.akvine.wild.bot.admin.dto.common.Response;
import ru.akvine.wild.bot.admin.dto.security.auth.AuthRequest;

@RequestMapping(value = "/security/one/factor")
public interface SecurityControllerMeta {
    @PostMapping(value = "/registration")
    Response registration(@RequestBody @Valid AuthRequest request, HttpServletRequest httpServletRequest);

    @PostMapping(value = "/auth")
    Response auth(@RequestBody @Valid AuthRequest request, HttpServletRequest httpServletRequest);

    @PostMapping(value = "/logout")
    Response logout(HttpServletRequest request);
}
