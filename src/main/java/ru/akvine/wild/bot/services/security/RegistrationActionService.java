package ru.akvine.wild.bot.services.security;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.constants.DbLockConstants;
import ru.akvine.wild.bot.entities.security.OtpActionEntity;
import ru.akvine.wild.bot.entities.security.RegistrationActionEntity;
import ru.akvine.wild.bot.enums.security.ActionState;
import ru.akvine.wild.bot.exceptions.security.NoMoreNewOtpAvailableException;
import ru.akvine.wild.bot.exceptions.security.registration.RegistrationNotStartedException;
import ru.akvine.wild.bot.exceptions.security.registration.RegistrationWrongStateException;
import ru.akvine.wild.bot.repositories.security.ActionRepository;
import ru.akvine.wild.bot.repositories.security.RegistrationActionRepository;
import ru.akvine.wild.bot.services.admin.SupportService;
import ru.akvine.wild.bot.services.domain.admin.SupportUserModel;
import ru.akvine.wild.bot.services.dto.security.registration.RegistrationActionRequest;
import ru.akvine.wild.bot.services.dto.security.registration.RegistrationActionResult;
import ru.akvine.wild.bot.services.dto.support.SupportCreate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationActionService extends OtpActionService<RegistrationActionEntity> {
    private final RegistrationActionRepository registrationActionRepository;
    private final SupportService supportService;

    @Value("${security.otp.action.lifetime.seconds}")
    private int otpActionLifetimeSeconds;
    @Value("${security.otp.max.invalid.attempts}")
    private int optMaxInvalidAttempts;
    @Value("${security.otp.max.new.generation.per.action}")
    private int otpMaxNewGenerationPerAction;

    public RegistrationActionResult startRegistration(RegistrationActionRequest request) {
        Preconditions.checkNotNull(request, "registrationActionRequest is null");

        String login = request.getEmail();
        String sessionId = request.getSessionId();

        verifyNotBlocked(login);

        RegistrationActionEntity registrationActionEntity = lockProvider.doWithLock(getLock(login), () -> {
            RegistrationActionEntity registrationAction = getRepository().findCurrentAction(login);
            if (registrationAction == null) {
                return createNewActionAndSendOtp(login, sessionId);
            }
            // Действие просрочено
            if (registrationAction.getOtpAction().isActionExpired()) {
                getRepository().delete(registrationAction);
                return createNewActionAndSendOtp(login, sessionId);
            }
            // Действие не просрочено и код еще годе ... вернем текущее состояние
            if (registrationAction.getOtpAction().getOtpExpiredAt() != null && registrationAction.getOtpAction().isNotExpiredOtp()) {
                return registrationAction;
            }
            // Пользователь не захотел вводить пароль, OTP уже было использовано, генерируем новый код
            if (registrationAction.getOtpAction().getOtpExpiredAt() == null) {
                getRepository().delete(registrationAction);
                return createNewActionAndSendOtp(login, sessionId);
            }
            // Код просрочен, но новый сгенерировать не можем - лимит исчерпан
            if (registrationAction.getOtpAction().isNewOtpLimitReached()) {
                handleNoMoreNewOtp(registrationAction);
                throw new NoMoreNewOtpAvailableException("No more one-time-password can be generated!");
            }

            // Действие не просрочено, но просрочен код, нужно сгенерировать нвоый - лимит еще есть
            return updateNewOtpAndSendToClient(registrationAction);
        });

        return buildActionInfo(registrationActionEntity);
    }

    public RegistrationActionResult checkOneTimePassword(RegistrationActionRequest request) {
        Preconditions.checkNotNull(request, "registrationAction is null");

        String login = request.getEmail();
        String sessionId = request.getSessionId();
        String otp = request.getOtp();

        Preconditions.checkNotNull(login, "login is null");
        Preconditions.checkNotNull(sessionId, "sessionId is null");
        Preconditions.checkNotNull(otp, "otp is null");

        verifyNotBlocked(login);

        RegistrationActionEntity registrationActionEntity = lockProvider.doWithLock(getLock(login), () -> {
            RegistrationActionEntity registrationAction = checkOtpInput(login, otp, sessionId);
            registrationAction.setState(ActionState.OTP_PASSED);
            registrationAction.getOtpAction().setOtpValueToNull();

            RegistrationActionEntity savedRegistrationAction = getRepository().save(registrationAction);
            logger.info("Client with email = {} was successfully passed otp!", login);
            return  savedRegistrationAction;
        });

        return buildActionInfo(registrationActionEntity);
    }

    public RegistrationActionResult generateNewOneTimePassword(RegistrationActionRequest request) {
        Preconditions.checkNotNull(request, "registrationActionRequest is null");
        Preconditions.checkNotNull(request.getEmail(), "registrationActionRequest.email is null");
        Preconditions.checkNotNull(request.getSessionId(), "registrationActionRequest.sessionId is null");
        return generateNewOtp(request.getEmail());
    }

    public SupportUserModel finishRegistration(RegistrationActionRequest request) {
        Preconditions.checkNotNull(request, "registrationActionRequest is null");

        String login = request.getEmail();
        verifyNotBlocked(login);

        return lockProvider.doWithLock(getLock(login), () -> {
            RegistrationActionEntity registrationAction = getRepository().findCurrentAction(login);
            if (registrationAction == null) {
                logger.info("Registration for email = {} not started yet!", login);
                throw new RegistrationNotStartedException("Registration not started yet");
            }

            // Действие просрочено
            if (registrationAction.getOtpAction().isActionExpired()) {
                getRepository().delete(registrationAction);
                logger.info("Registration for email = {} not started yet!", login);
                throw new RegistrationNotStartedException("Registration not started yet!");
            }

            verifySession(registrationAction, request.getSessionId());
            verifyState(ActionState.OTP_PASSED, registrationAction);

            getRepository().delete(registrationAction);

            SupportCreate supportCreate = new SupportCreate()
                    .setEmail(login)
                    .setPassword(request.getPassword());
            return supportService.create(supportCreate);
        });
    }

    protected RegistrationActionEntity createNewActionAndSendOtp(String login, String sessionId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime actionExpiredAt = now.plusSeconds(otpActionLifetimeSeconds);

        OtpActionEntity otp = new OtpActionEntity()
                .setStartedDate(now)
                .setActionExpiredAt(actionExpiredAt)
                .setOtpInvalidAttemptsLeft(optMaxInvalidAttempts)
                .setOtpCountLeft(otpMaxNewGenerationPerAction);

        RegistrationActionEntity registrationAction = new RegistrationActionEntity()
                .setLogin(login)
                .setSessionId(sessionId)
                .setOtpAction(otp);
        return updateNewOtpAndSendToClient(registrationAction);
    }

    @Override
    protected String getActionName() {
        return "registration-action";
    }

    @Override
    protected String getLock(String payload) {
        return DbLockConstants.REG_PREFIX + payload;
    }

    @Override
    protected ActionRepository<RegistrationActionEntity> getRepository() {
        return registrationActionRepository;
    }

    @Override
    protected void sendNewOtpToClient(RegistrationActionEntity action) {
        String login = action.getLogin();
        twoFactorNotificationSender.sendRegistrationCode(login, action.getOtpAction().getOtpValue());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected RegistrationActionResult buildActionInfo(RegistrationActionEntity action) {
        return new RegistrationActionResult(action);
    }

    private void verifyState(ActionState expectedState, RegistrationActionEntity registrationActionEntity) {
        if (registrationActionEntity.getState() == expectedState) {
            return;
        }

        String errorMessage = String.format("Registration for login=[%s] must be in state=[%s]",
                registrationActionEntity.getLogin(), expectedState);
        throw new RegistrationWrongStateException(errorMessage);
    }
}
