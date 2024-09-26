package ru.akvine.wild.bot.services.security;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.constants.DbLockConstants;
import ru.akvine.wild.bot.entities.security.AuthActionEntity;
import ru.akvine.wild.bot.entities.security.OtpActionEntity;
import ru.akvine.wild.bot.exceptions.security.NoMoreNewOtpAvailableException;
import ru.akvine.wild.bot.repositories.security.ActionRepository;
import ru.akvine.wild.bot.repositories.security.AuthActionRepository;
import ru.akvine.wild.bot.services.domain.admin.SupportUserModel;
import ru.akvine.wild.bot.services.dto.security.OtpCreateNewAction;
import ru.akvine.wild.bot.services.dto.security.auth.AuthActionRequest;
import ru.akvine.wild.bot.services.dto.security.auth.AuthActionResult;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthActionService extends PasswordRequiredActionService<AuthActionEntity> {
    private final AuthActionRepository authActionRepository;

    @Value("${security.otp.action.lifetime.seconds}")
    private int otpActionLifetimeSeconds;
    @Value("${security.otp.password.max.invalid.attempts}")
    private int otpPasswordMaxInvalidAttempts;
    @Value("${security.otp.max.invalid.attempts}")
    private int otpMaxInvalidAttempts;
    @Value("${security.otp.max.new.generation.per.action}")
    private int otpMaxNewGenerationPerAction;

    public AuthActionResult startAuth(AuthActionRequest request) {
        Preconditions.checkNotNull(request, "authActionRequest is null");

        String login = request.getEmail();
        String password = request.getPassword();
        String sessionId = request.getSessionId();

        verifyNotBlocked(login);
        SupportUserModel clientBean = supportService.getByEmail(login);
        final boolean isPasswordValid = isValidPassword(clientBean, password);

        AuthActionEntity authActionEntity = lockProvider.doWithLock(getLock(login), () -> {
            AuthActionEntity authAction = getRepository().findCurrentAction(login);
            if (authAction == null) {
                OtpCreateNewAction otpCreateNewAction = new OtpCreateNewAction(login, sessionId, isPasswordValid);
                return createNewActionAndSendOtp(otpCreateNewAction);
            }

            // Действие просрочено
            if (authAction.getOtpAction().isActionExpired()) {
                getRepository().delete(authAction);
                OtpCreateNewAction otpCreateNewAction = new OtpCreateNewAction(login, sessionId, isPasswordValid);
                return createNewActionAndSendOtp(otpCreateNewAction);
            }
            if (!isPasswordValid) {
                handleInvalidPasswordInput(authAction);
            }
            // otp не был сгенерирован, т.к. вводили неправильный пароль
            if (authAction.getOtpAction().getOtpNumber() == null) {
                return updateNewOtpAndSend(authAction);
            }
            // Действие не просрочено и код еще годен ... вернем текущее состояние
            if (authAction.getOtpAction().isNotExpiredOtp()) {
                return authAction;
            }
            // Код просрочен, но новый сгенерировать не можем - лимит исчерпан
            if (authAction.getOtpAction().isNewOtpLimitReached()) {
                handleNoMoreNewOtp(authAction);
                throw new NoMoreNewOtpAvailableException("No more one-time-password can be generated!");
            }

            // Действие не просрочено, но просрочен код, нужно сгенерировать новый - лимит еще есть
            return updateNewOtpAndSendToClient(authAction);
        });

        return buildActionInfo(authActionEntity);
    }

    public SupportUserModel finishAuth(AuthActionRequest request) {
        Preconditions.checkNotNull(request, "authActionRequest is null");

        String login = request.getEmail();
        String otp = request.getOtp();
        String sessionId = request.getSessionId();

        verifyNotBlocked(login);
        SupportUserModel clientBean = supportService.getByEmail(login);

        return lockProvider.doWithLock((getLock(login)), () -> {
            AuthActionEntity authActionEntity = checkOtpInput(login, otp, sessionId);
            logger.info("Client with email = {} successfully passed otp!", login);
            getRepository().delete(authActionEntity);
            return clientBean;
        });
    }

    @Override
    protected AuthActionEntity createNewActionAndSendOtp(OtpCreateNewAction otpCreateNewAction) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime actionExpiredAt = now.plusSeconds(otpActionLifetimeSeconds);

        AuthActionEntity authActionEntity = new AuthActionEntity()
                .setLogin(otpCreateNewAction.getLogin())
                .setSessionId(otpCreateNewAction.getSessionId())
                .setPwdInvalidAttemptsLeft(otpPasswordMaxInvalidAttempts);

        OtpActionEntity otp = new OtpActionEntity()
                .setStartedDate(now)
                .setActionExpiredAt(actionExpiredAt)
                .setOtpInvalidAttemptsLeft(otpMaxInvalidAttempts)
                .setOtpCountLeft(otpMaxNewGenerationPerAction);
        authActionEntity.setOtpAction(otp);

        if (!otpCreateNewAction.isCredentialsValid()) {
            authActionEntity.decrementPwdInvalidAttemptsLeft();
            getRepository().save(authActionEntity);
            throw new BadCredentialsException("Invalid password");
        }
        return updateNewOtpAndSend(authActionEntity);
    }

    @Override
    protected String getActionName() {
        return "auth-action";
    }

    @Override
    protected String getLock(String payload) {
        return DbLockConstants.AUTH_PREFIX + payload;
    }

    @Override
    protected ActionRepository<AuthActionEntity> getRepository() {
        return authActionRepository;
    }

    @Override
    protected void sendNewOtpToClient(AuthActionEntity action) {
        String login = action.getLogin();
        twoFactorNotificationSender.sendAuthenticationCode(login, action.getOtpAction().getOtpValue());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected AuthActionResult buildActionInfo(AuthActionEntity action) {
        return new AuthActionResult(action);
    }
}
