package ru.akvine.wild.bot.admin.converters.security;

import com.google.common.base.Preconditions;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.admin.dto.security.OtpActionResponse;
import ru.akvine.wild.bot.admin.dto.security.registration.*;
import ru.akvine.wild.bot.helpers.SecurityHelper;
import ru.akvine.wild.bot.services.dto.security.registration.RegistrationActionRequest;
import ru.akvine.wild.bot.services.dto.security.registration.RegistrationActionResult;

@Component
@RequiredArgsConstructor
public class RegistrationConverter {
    @Value("${security.otp.new.delay.seconds}")
    private long otpNewDelaySeconds;

    private final SecurityHelper securityHelper;

    public RegistrationActionRequest convertToRegistrationActionRequest(RegistrationStartRequest request,
                                                                        HttpServletRequest httpServletRequest) {
        Preconditions.checkNotNull(request, "registrationStartRequest is null");
        Preconditions.checkNotNull(httpServletRequest, "httpServletRequest is null");
        return new RegistrationActionRequest()
                .setEmail(request.getEmail())
                .setSessionId(httpServletRequest.getSession(true).getId());
    }

    public RegistrationActionRequest convertToRegistrationActionRequest(RegistrationCheckOtpRequest request,
                                                                        HttpServletRequest httpServletRequest) {
        Preconditions.checkNotNull(request, "registrationCheckOtpRequest is null");
        Preconditions.checkNotNull(httpServletRequest, "httpServletRequest is null");

        return new RegistrationActionRequest()
                .setEmail(request.getEmail())
                .setSessionId(securityHelper.getSession(httpServletRequest).getId())
                .setOtp(request.getOtp());
    }

    public RegistrationActionRequest convertToRegistrationActionRequest(RegistrationNewOtpRequest request,
                                                                        HttpServletRequest httpServletRequest) {
        Preconditions.checkNotNull(request, "registrationNewOtpRequest is null");
        Preconditions.checkNotNull(httpServletRequest, "httpServletRequest is null");
        return new RegistrationActionRequest()
                .setEmail(request.getEmail())
                .setSessionId(securityHelper.getSession(httpServletRequest).getId());
    }

    public RegistrationActionRequest convertToRegistrationActionRequest(RegistrationFinishRequest request,
                                                                        HttpServletRequest httpServletRequest) {
        Preconditions.checkNotNull(request, "registrationFinishRequest is null");
        Preconditions.checkNotNull(httpServletRequest, "httpServletRequest is null");

        return new RegistrationActionRequest()
                .setEmail(request.getEmail())
                .setSessionId(securityHelper.getSession(httpServletRequest).getId())
                .setPassword(request.getPassword());
    }

    public RegistrationResponse convertToRegistrationResponse(RegistrationActionResult result) {
        Preconditions.checkNotNull(result, "registrationActionResult is null");

        OtpActionResponse otpActionResponse = new OtpActionResponse()
                .setActionExpiredAt(result.getOtp().getExpiredAt().toString())
                .setOtpCountLeft(result.getOtp().getOtpCountLeft())
                .setOtpNumber(result.getOtp().getOtpNumber())
                .setOtpLastUpdate(result.getOtp().getOtpLastUpdate() != null ? result.getOtp().getOtpLastUpdate().toString() : null)
                .setNewOtpDelay(otpNewDelaySeconds)
                .setOtpInvalidAttemptsLeft(result.getOtp().getOtpInvalidAttemptsLeft());

        return new RegistrationResponse()
                .setOtp(otpActionResponse)
                .setState(result.getState().name());
    }
}
