package ru.akvine.wild.bot.admin.impl.security.two_factor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.akvine.wild.bot.admin.converters.security.AccessRestoreConverter;
import ru.akvine.wild.bot.admin.dto.common.Response;
import ru.akvine.wild.bot.admin.dto.common.SuccessfulResponse;
import ru.akvine.wild.bot.admin.dto.security.access_restore.AccessRestoreCheckOtpRequest;
import ru.akvine.wild.bot.admin.dto.security.access_restore.AccessRestoreFinishRequest;
import ru.akvine.wild.bot.admin.dto.security.access_restore.AccessRestoreStartRequest;
import ru.akvine.wild.bot.admin.meta.security.two_factor.AccessRestoreControllerMeta;
import ru.akvine.wild.bot.admin.validator.security.AccessRestoreValidator;
import ru.akvine.wild.bot.helpers.SecurityHelper;
import ru.akvine.wild.bot.services.domain.admin.SupportUserModel;
import ru.akvine.wild.bot.services.dto.security.access_restore.AccessRestoreActionRequest;
import ru.akvine.wild.bot.services.dto.security.access_restore.AccessRestoreActionResult;
import ru.akvine.wild.bot.services.security.AccessRestoreActionService;

@RestController
@RequiredArgsConstructor
public class AccessRestoreController implements AccessRestoreControllerMeta {
    private final AccessRestoreActionService accessRestoreActionService;
    private final AccessRestoreValidator accessRestoreValidator;
    private final AccessRestoreConverter accessRestoreConverter;
    private final SecurityHelper securityHelper;

    @Override
    public Response start(@Valid AccessRestoreStartRequest request, HttpServletRequest httpServletRequest) {
        accessRestoreValidator.verifyAccessRestore(request);
        AccessRestoreActionRequest actionRequest = accessRestoreConverter.convertToAccessRestoreActionRequest(request, httpServletRequest);
        AccessRestoreActionResult result = accessRestoreActionService.startAccessRestore(actionRequest);
        return accessRestoreConverter.convertToAccessRestoreResponse(result);
    }

    @Override
    public Response newotp(@Valid AccessRestoreStartRequest request, HttpServletRequest httpServletRequest) {
        accessRestoreValidator.verifyAccessRestore(request);
        AccessRestoreActionRequest actionRequest = accessRestoreConverter.convertToAccessRestoreActionRequest(request, httpServletRequest);
        AccessRestoreActionResult result = accessRestoreActionService.generateNewOneTimePassword(actionRequest);
        return accessRestoreConverter.convertToAccessRestoreResponse(result);
    }

    @Override
    public Response check(@Valid AccessRestoreCheckOtpRequest request, HttpServletRequest httpServletRequest) {
        accessRestoreValidator.verifyAccessRestore(request);
        AccessRestoreActionRequest actionRequest = accessRestoreConverter.convertToAccessRestoreActionRequest(request, httpServletRequest);
        AccessRestoreActionResult result = accessRestoreActionService.checkOneTimePassword(actionRequest);
        return accessRestoreConverter.convertToAccessRestoreResponse(result);
    }

    @Override
    public Response finish(@Valid AccessRestoreFinishRequest request, HttpServletRequest httpServletRequest) {
        accessRestoreValidator.verifyAccessRestoreFinish(request);
        AccessRestoreActionRequest actionRequest = accessRestoreConverter.convertToAccessRestoreActionRequest(request, httpServletRequest);
        SupportUserModel supportUser = accessRestoreActionService.finishAccessRestore(actionRequest);
        securityHelper.authenticate(supportUser, httpServletRequest);
        return new SuccessfulResponse();
    }
}
