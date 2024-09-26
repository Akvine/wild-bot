package ru.akvine.wild.bot.admin.meta.security.two_factor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.akvine.wild.bot.admin.dto.common.Response;
import ru.akvine.wild.bot.admin.dto.security.registration.*;

@RequestMapping("/security/two/factor/registration")
public interface RegistrationControllerMeta {
    @PostMapping(value = "/start")
    Response start(@Valid @RequestBody RegistrationStartRequest request, HttpServletRequest httpServletRequest);

    @PostMapping(value = "/check")
    Response check(@Valid @RequestBody RegistrationCheckOtpRequest request, HttpServletRequest httpServletRequest);

    @PostMapping(value = "/newotp")
    Response newotp(@Valid @RequestBody RegistrationNewOtpRequest request, HttpServletRequest httpServletRequest);

    @PostMapping(value = "/password/validate")
    Response passwordValidate(@Valid @RequestBody RegistrationPasswordValidateRequest request);

    @PostMapping(value = "/finish")
    Response finish(@Valid @RequestBody RegistrationFinishRequest request, HttpServletRequest httpServletRequest);
}
