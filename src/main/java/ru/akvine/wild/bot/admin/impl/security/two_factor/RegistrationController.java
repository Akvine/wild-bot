package ru.akvine.wild.bot.admin.impl.security.two_factor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.akvine.wild.bot.admin.converters.security.RegistrationConverter;
import ru.akvine.wild.bot.admin.dto.common.Response;
import ru.akvine.wild.bot.admin.dto.common.SuccessfulResponse;
import ru.akvine.wild.bot.admin.dto.security.registration.*;
import ru.akvine.wild.bot.admin.meta.security.two_factor.RegistrationControllerMeta;
import ru.akvine.wild.bot.admin.validator.security.RegistrationValidator;
import ru.akvine.wild.bot.helpers.SecurityHelper;
import ru.akvine.wild.bot.services.domain.admin.SupportUserModel;
import ru.akvine.wild.bot.services.dto.security.registration.RegistrationActionRequest;
import ru.akvine.wild.bot.services.dto.security.registration.RegistrationActionResult;
import ru.akvine.wild.bot.services.security.RegistrationActionService;

@RestController
@RequiredArgsConstructor
public class RegistrationController implements RegistrationControllerMeta {
    private final RegistrationValidator registrationValidator;
    private final RegistrationConverter registrationConverter;
    private final RegistrationActionService registrationActionService;
    private final SecurityHelper securityHelper;


    @Override
    public Response start(@Valid RegistrationStartRequest request, HttpServletRequest httpServletRequest) {
        registrationValidator.verifyRegistrationLogin(request);
        RegistrationActionRequest registrationActionRequest = registrationConverter.convertToRegistrationActionRequest(request, httpServletRequest);
        RegistrationActionResult registrationActionResult = registrationActionService.startRegistration(registrationActionRequest);
        return registrationConverter.convertToRegistrationResponse(registrationActionResult);
    }

    @Override
    public Response check(@Valid RegistrationCheckOtpRequest request, HttpServletRequest httpServletRequest) {
        registrationValidator.verifyRegistrationLogin(request);
        RegistrationActionRequest registrationActionRequest = registrationConverter.convertToRegistrationActionRequest(request, httpServletRequest);
        RegistrationActionResult registrationActionResult = registrationActionService.checkOneTimePassword(registrationActionRequest);
        return registrationConverter.convertToRegistrationResponse(registrationActionResult);
    }

    @Override
    public Response newotp(@Valid RegistrationNewOtpRequest request, HttpServletRequest httpServletRequest) {
        registrationValidator.verifyRegistrationLogin(request);
        RegistrationActionRequest registrationActionRequest = registrationConverter.convertToRegistrationActionRequest(request, httpServletRequest);
        RegistrationActionResult registrationActionResult = registrationActionService.generateNewOneTimePassword(registrationActionRequest);
        return registrationConverter.convertToRegistrationResponse(registrationActionResult);
    }

    @Override
    public Response passwordValidate(@Valid RegistrationPasswordValidateRequest request) {
        registrationValidator.verifyRegistrationPassword(request);
        return new SuccessfulResponse();
    }

    @Override
    public Response finish(@Valid RegistrationFinishRequest request, HttpServletRequest httpServletRequest) {
        registrationValidator.verifyRegistrationFinish(request);
        RegistrationActionRequest registrationActionRequest = registrationConverter.convertToRegistrationActionRequest(request, httpServletRequest);
        SupportUserModel supportUser = registrationActionService.finishRegistration(registrationActionRequest);
        securityHelper.authenticate(supportUser, httpServletRequest);
        return new SuccessfulResponse();
    }
}
