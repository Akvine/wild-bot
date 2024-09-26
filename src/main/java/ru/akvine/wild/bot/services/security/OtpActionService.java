package ru.akvine.wild.bot.services.security;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.entities.security.OneTimePasswordable;
import ru.akvine.wild.bot.entities.security.OtpActionEntity;
import ru.akvine.wild.bot.entities.security.OtpInfo;
import ru.akvine.wild.bot.exceptions.security.*;
import ru.akvine.wild.bot.infrastructure.lock.distributed.DataBaseLockProvider;
import ru.akvine.wild.bot.repositories.security.ActionRepository;
import ru.akvine.wild.bot.services.notification.TwoFactorNotificationSender;

import java.time.LocalDateTime;

@Service
@Slf4j
public abstract class OtpActionService<T extends OneTimePasswordable> {
    @Autowired
    protected DataBaseLockProvider lockProvider;
    @Autowired
    protected SupportBlockingService supportBlockingService;
    @Autowired
    protected OtpService otpService;
    @Autowired
    protected TwoFactorNotificationSender twoFactorNotificationSender;

    @Value("${security.otp.new.delay.seconds}")
    private long otpDelaySeconds;

    public <R> R generateNewOtp(String payload) {
        Preconditions.checkNotNull(payload, "payload is null");

        String lockId = getLock(payload);

        T otpAction = lockProvider.doWithLock(lockId, () -> {
            T action = getRepository().findCurrentAction(payload);
            if (action == null) {
                logger.info("Client tried to get new otp, but {} is not initiated", getActionName());
                throw new ActionNotStartedException("Can'[t generate new one-time-password, action not started!");
            }

            verifyNotBlocked(action.getLogin());

            // Действие просрочено
            if (action.getOtpAction().isActionExpired()) {
                logger.info("Client with email = {} tried to get new otp, but {} is expired", action.getLogin(), getActionName());
                getRepository().delete(action);
                logger.info("Expired {}[id = {}] removed from DB", getActionName(), action.getId());
                throw new ActionNotStartedException("Can't generate new one-time-password, action not started!");
            }

            if (noCurrentOtpInfoAvailable(action)) {
                return action;
            }

            // Действие не просрочено и не прошла задержка между генерациями
            if (newOtpDelayIsNotPassed(action)) {
                return action;
            }
            // Задержка прошла, но новый сгенерировать не можем - лимит исчерпан
            if (action.getOtpAction().isNewOtpLimitReached()) {
                handleNoMoreNewOtp(action);
                throw new NoMoreNewOtpAvailableException("No more one-time-password can be generate!");
            }

            // Действие не просрчоено, задержка прошла, лимит не исчерпан - генериурем новый код
            return updateNewOtpAndSendToClient(action);
        });

        return buildActionInfo(otpAction);
    }

    protected T checkOtpInput(String payload, String clientInput, String sessionId) {
        T action = getRepository().findCurrentAction(payload);
        if (action == null) {
            logger.info("User tried to finish {}, but it is not initiated", getActionName());
            throw new ActionNotStartedException("Action not started!");
        }

        verifySession(action, sessionId);

        String login = action.getLogin();
        // Действие просрочено
        if (action.getOtpAction().isActionExpired()) {
            logger.info("User with email = {} tried to finish {}, but action is expired!", login, getActionName());
            getRepository().delete(action);
            logger.info("Expired {}[id = {}] removed from DB", getActionName(), action.getId());
            throw new ActionNotStartedException(String.format("Can't finish %s, action is expired!", getActionName()));
        }

        // Действие не просрочено, но просрочен код
        if (action.getOtpAction().isExpiredOtp()) {
            logger.info("User with email = {} tried to finish {}, but otp is expired! New otp left = {}", login, getActionName(),
                    action.getOtpAction().getOtpCountLeft());
            throw new OtpExpiredException(action.getOtpAction().getOtpCountLeft());
        }
        // Действие не просрочено и код еще активен - проверяем
        if (!action.getOtpAction().isOtpValid(clientInput)) { // Неверный ввод
            int otpInvalidAttemptsLeft = action.getOtpAction().decrementInvalidAttemptsLeft();
            T updatedAction = getRepository().save(action);
            if (otpInvalidAttemptsLeft == 0) {
                handleNoMoreOtpInvalidAttemptsLeft(updatedAction);
                throw new BlockedCredentialsException(login);
            }

            handleOtpInvalidAttempt(login, otpInvalidAttemptsLeft);
        }

        return action;
    }

    protected void verifyNotBlocked(String login) {
        LocalDateTime unblockDate = supportBlockingService.getUnblockDate(login);
        if (unblockDate == null || unblockDate.isBefore(LocalDateTime.now())) {
            return;
        }

        logger.info("Blocked email = {} trying to perform action = {}. Block until date = {}", login, getActionName(), unblockDate);
        throw new BlockedCredentialsException(login);
    }

    protected void handleNoMoreNewOtp(T action) {
        String login = action.getLogin();
        supportBlockingService.setBlock(login);
        logger.info("Client with email = {} reached limit of maximum otp generation for {} and set blocked!", login, getActionName());
        getRepository().delete(action);
        logger.info("Blocked client's action = {} for email = {}[id = {}] removed from DB", login, getActionName(), action.getId());
    }

    protected T updateNewOtpAndSendToClient(T action) {
        String login = action.getLogin();

        OtpInfo newOtpInfo = otpService.getOneTimePassword(login);
        action.getOtpAction().setNewOtpValue(newOtpInfo);

        sendNewOtpToClient(action);
        return getRepository().save(action);
    }

    protected abstract String getActionName();
    protected abstract String getLock(String payload);
    protected abstract ActionRepository<T> getRepository();
    protected abstract void sendNewOtpToClient(T action);
    protected abstract <R> R buildActionInfo(T action);

    protected final boolean noCurrentOtpInfoAvailable(T action) {
        OtpActionEntity otpAction = action.getOtpAction();
        return otpAction.getOtpNumber() == null
                || StringUtils.isBlank(otpAction.getOtpValue())
                || otpAction.getOtpExpiredAt() == null
                || otpAction.getOtpLastUpdate() == null;
    }

    private boolean newOtpDelayIsNotPassed(T passwordChangeAction) {
        LocalDateTime generationAllowedTime = passwordChangeAction
                .getOtpAction()
                .getOtpLastUpdate()
                .plusSeconds(otpDelaySeconds);
        return LocalDateTime.now().isBefore(generationAllowedTime);
    }

    protected void verifySession(T action, String sessionId) {
        String regSessionId = action.getSessionId();
        if (regSessionId == null) {
            return;
        }

        if (sessionId != null && sessionId.equals(regSessionId)) {
            return;
        }

        logger.info("{} for email = {} can't be processed. Wrong session!", getActionName(), action.getLogin());
        throw new WrongSessionException("Wrong session");
    }

    protected void handleNoMoreOtpInvalidAttemptsLeft(T action) {
        String login = action.getLogin();
        supportBlockingService.setBlock(login);
        logger.info("Client with email = {} reached limit for invalid otp input and set blocked!", login);
        getRepository().delete(action);
        logger.info("Blocked client's action = {} for email = {}[id = {}] removed from DB", login, getActionName(), action.getId());
    }

    protected void handleOtpInvalidAttempt(String login, int otpInvalidAttemptsLeft) {
        throw new OtpInvalidAttemptException(login, otpInvalidAttemptsLeft);
    }
}
