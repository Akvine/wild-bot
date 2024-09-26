package ru.akvine.wild.bot.services.security;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.entities.security.AccountPasswordable;
import ru.akvine.wild.bot.entities.security.OneTimePasswordable;
import ru.akvine.wild.bot.entities.security.OtpInfo;
import ru.akvine.wild.bot.services.admin.SupportService;
import ru.akvine.wild.bot.services.domain.admin.SupportUserModel;
import ru.akvine.wild.bot.services.dto.security.OtpCreateNewAction;

@Service
@Slf4j
public abstract class PasswordRequiredActionService <T extends AccountPasswordable & OneTimePasswordable> extends OtpActionService<T> {
    @Autowired
    protected SupportService supportService;
    @Autowired
    protected PasswordService passwordService;

    protected boolean isValidPassword(SupportUserModel supportUser, String password) {
        Preconditions.checkNotNull(supportUser, "supportUser is null");
        Preconditions.checkNotNull(password, "password is null");
        return passwordService.isValidPassword(supportUser, password);
    }

    protected void handleInvalidPasswordInput(T action) {
        action.decrementPwdInvalidAttemptsLeft();
        if (action.getPwdInvalidAttemptsLeft() <= 0) {
            handleNoMorePasswordInvalidAttemptsLeft(action);
            throw new BadCredentialsException("Client reached limit of password invalid attempts");
        } else {
            action = getRepository().save(action);
            logger.info("Client with email = {} tried to initiate {}, but entered wrong account password. Invalid attempts left = {}",
                    action.getLogin(), getActionName(), action.getPwdInvalidAttemptsLeft());
            throw new BadCredentialsException("Invalid password");
        }
    }

    protected void handleNoMorePasswordInvalidAttemptsLeft(T action) {
        String login = action.getLogin();
        blockingService.setBlock(login);
        logger.info("Client with email = {} reached limit for invalid password input and set blocked", login);
        getRepository().delete(action);
        logger.info("Blocked client's with email = {} {}[id={}] removed from DB", login, getActionName(), action.getId());
    }

    protected abstract T createNewActionAndSendOtp(OtpCreateNewAction otpCreateNewAction);

    protected T updateNewOtpAndSend(T action) {
        String login = action.getLogin();
        OtpInfo newOtpInfo = otpService.getOneTimePassword(login);

        action.getOtpAction().setNewOtpValue(newOtpInfo);
        sendNewOtpToClient(action);
        return getRepository().save(action);
    }
}
