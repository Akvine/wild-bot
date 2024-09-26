package ru.akvine.wild.bot.admin.impl.security.one_factor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.akvine.wild.bot.admin.dto.common.Response;
import ru.akvine.wild.bot.admin.dto.common.SuccessfulResponse;
import ru.akvine.wild.bot.admin.dto.security.auth.AuthRequest;
import ru.akvine.wild.bot.admin.meta.security.one_factor.SecurityControllerMeta;
import ru.akvine.wild.bot.helpers.SecurityHelper;
import ru.akvine.wild.bot.services.admin.SupportService;
import ru.akvine.wild.bot.services.domain.admin.SupportUserModel;
import ru.akvine.wild.bot.services.security.AuthenticationService;

@RestController
@RequiredArgsConstructor
public class SecurityControllerImpl implements SecurityControllerMeta {
    private final SupportService supportService;
    private final SecurityHelper securityHelper;
    private final AuthenticationService authenticationService;

    @Override
    public Response registration(@RequestBody @Valid AuthRequest request,
                                 HttpServletRequest httpServletRequest) {
        SupportUserModel supportUser = supportService.create(request.getEmail(), request.getPassword());
        securityHelper.authenticate(supportUser, httpServletRequest);
        return new SuccessfulResponse();
    }

    @Override
    public Response auth(@RequestBody @Valid AuthRequest request,
                         HttpServletRequest httpServletRequest) {
        SupportUserModel supportUser = authenticationService.authenticate(
                request.getEmail(),
                request.getPassword());
        securityHelper.authenticate(supportUser, httpServletRequest);
        return new SuccessfulResponse();
    }

    @Override
    public Response logout(HttpServletRequest request) {
        securityHelper.doLogout(request);
        return new SuccessfulResponse();
    }
}
