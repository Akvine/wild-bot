package ru.akvine.wild.bot.admin.converters.security;

import com.google.common.base.Preconditions;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.admin.dto.security.OtpActionResponse;
import ru.akvine.wild.bot.admin.dto.security.access_restore.AccessRestoreCheckOtpRequest;
import ru.akvine.wild.bot.admin.dto.security.access_restore.AccessRestoreFinishRequest;
import ru.akvine.wild.bot.admin.dto.security.access_restore.AccessRestoreResponse;
import ru.akvine.wild.bot.admin.dto.security.access_restore.AccessRestoreStartRequest;
import ru.akvine.wild.bot.helpers.SecurityHelper;
import ru.akvine.wild.bot.services.dto.security.access_restore.AccessRestoreActionRequest;
import ru.akvine.wild.bot.services.dto.security.access_restore.AccessRestoreActionResult;

@Component
@RequiredArgsConstructor
public class AccessRestoreConverter {

    @Value("${security.otp.new.delay.seconds}")
    private long otpNewDelaySeconds;

    private final SecurityHelper securityHelper;

    public AccessRestoreActionRequest convertToAccessRestoreActionRequest(AccessRestoreStartRequest request,
                                                                          HttpServletRequest httpServletRequest) {
        Preconditions.checkNotNull(request, "accessRestoreStartRequest is null");
        Preconditions.checkNotNull(httpServletRequest, "httpServletRequest is null");
        return new AccessRestoreActionRequest()
                .setLogin(request.getEmail())
                .setSessionId(httpServletRequest.getSession(true).getId());
    }

    public AccessRestoreActionRequest convertToAccessRestoreActionRequest(AccessRestoreCheckOtpRequest request,
                                                                          HttpServletRequest httpServletRequest) {
        Preconditions.checkNotNull(request, "accessRestoreCheckOtpRequest is null");
        Preconditions.checkNotNull(httpServletRequest, "httpServletRequest is null");

        return new AccessRestoreActionRequest()
                .setLogin(request.getEmail())
                .setSessionId(securityHelper.getSession(httpServletRequest).getId())
                .setOtp(request.getOtp());
    }

    public AccessRestoreActionRequest convertToAccessRestoreActionRequest(AccessRestoreFinishRequest request,
                                                                          HttpServletRequest httpServletRequest) {
        Preconditions.checkNotNull(request, "accessRestoreFinishRequest is null");
        Preconditions.checkNotNull(httpServletRequest, "httpServletRequest is null");
        return new AccessRestoreActionRequest()
                .setLogin(request.getEmail())
                .setSessionId(securityHelper.getSession(httpServletRequest).getId())
                .setPassword(request.getPassword());
    }

    public AccessRestoreResponse convertToAccessRestoreResponse(AccessRestoreActionResult result) {
        Preconditions.checkNotNull(result, "accessRestoreActionResult is null");
        Preconditions.checkNotNull(result.getState(), "accessRestoreActionResult.state is null");
        Preconditions.checkNotNull(result.getOtp(), "accessRestoreActionResult.otp is null");

        OtpActionResponse otpActionResponse = new OtpActionResponse()
                .setActionExpiredAt(result.getOtp().getExpiredAt().toString())
                .setOtpCountLeft(result.getOtp().getOtpCountLeft())
                .setOtpNumber(result.getOtp().getOtpNumber())
                .setOtpLastUpdate(result.getOtp().getOtpLastUpdate() != null ? result.getOtp().getOtpLastUpdate().toString() : null)
                .setNewOtpDelay(otpNewDelaySeconds)
                .setOtpInvalidAttemptsLeft(result.getOtp().getOtpInvalidAttemptsLeft());

        return new AccessRestoreResponse()
                .setState(result.getState().toString())
                .setOtp(otpActionResponse);
    }
}
