package ru.akvine.wild.bot.admin.impl.security.two_factor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.akvine.wild.bot.admin.converters.security.AuthConverter;
import ru.akvine.wild.bot.admin.dto.common.Response;
import ru.akvine.wild.bot.admin.dto.common.SuccessfulResponse;
import ru.akvine.wild.bot.admin.dto.security.auth.AuthCredentialsRequest;
import ru.akvine.wild.bot.admin.dto.security.auth.AuthFinishRequest;
import ru.akvine.wild.bot.admin.dto.security.auth.AuthNewOtpRequest;
import ru.akvine.wild.bot.admin.meta.security.two_factor.AuthControllerMeta;
import ru.akvine.wild.bot.admin.validator.security.AuthValidator;
import ru.akvine.wild.bot.helpers.SecurityHelper;
import ru.akvine.wild.bot.services.domain.admin.SupportUserModel;
import ru.akvine.wild.bot.services.dto.security.auth.AuthActionRequest;
import ru.akvine.wild.bot.services.dto.security.auth.AuthActionResult;
import ru.akvine.wild.bot.services.security.AuthActionService;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthControllerMeta {
    private final AuthConverter authConverter;
    private final AuthValidator authValidator;
    private final AuthActionService authActionService;
    private final SecurityHelper securityHelper;

    @Override
    public Response start(@Valid AuthCredentialsRequest request, HttpServletRequest httpServletRequest) {
        authValidator.verifyAuthLogin(request);
        AuthActionRequest authActionRequest = authConverter.convertToAuthActionRequest(request, httpServletRequest);
        AuthActionResult authActionResult = authActionService.startAuth(authActionRequest);
        return authConverter.convertToAuthResponse(authActionResult);
    }

    @Override
    public Response newotp(@Valid AuthNewOtpRequest request, HttpServletRequest httpServletRequest) {
        authValidator.verifyAuthLogin(request);
        AuthActionResult authActionResult = authActionService.generateNewOtp(request.getEmail());
        return authConverter.convertToAuthResponse(authActionResult);
    }

    @Override
    public Response finish(@Valid @RequestBody AuthFinishRequest request, HttpServletRequest httpServletRequest) {
        authValidator.verifyAuthLogin(request);
        AuthActionRequest authActionRequest = authConverter.convertToAuthActionRequest(request, httpServletRequest);
        SupportUserModel supportUser = authActionService.finishAuth(authActionRequest);
        securityHelper.authenticate(supportUser, httpServletRequest);
        return new SuccessfulResponse();
    }

    @Override
    public Response logout(HttpServletRequest httpServletRequest) {
        securityHelper.doLogout(httpServletRequest);
        return new SuccessfulResponse();
    }
}
