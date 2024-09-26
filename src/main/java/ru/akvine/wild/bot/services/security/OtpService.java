package ru.akvine.wild.bot.services.security;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.akvine.wild.bot.entities.security.OtpCounterEntity;
import ru.akvine.wild.bot.entities.security.OtpInfo;
import ru.akvine.wild.bot.repositories.security.OtpCounterRepository;
import ru.akvine.wild.bot.services.notification.TwoFactorNotificationSender;
import ru.akvine.wild.bot.services.notification.dummy.ConstantTwoFactorNotificationSender;
import ru.akvine.wild.bot.utils.OneTimePasswordGenerator;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {
    private final TwoFactorNotificationSender twoFactorNotificationSender;
    private final OtpCounterRepository otpCounterRepository;

    @Value("${security.otp.length}")
    private int otpLength;
    @Value("${security.otp.max.invalid.attempts}")
    private int otpMaxInvalidAttempts;
    @Value("${security.otp.lifetime.seconds}")
    private long otpLifetimeSeconds;
    @Value("${security.notification.constant.dummy.code}")
    private String dummyCode;

    public OtpInfo getOneTimePassword(String login) {
        Preconditions.checkNotNull(login, "login is null");

        String value;
        if (twoFactorNotificationSender instanceof ConstantTwoFactorNotificationSender) {
            value = dummyCode;
        } else {
            value = OneTimePasswordGenerator.generate(otpLength);
        }
        int orderNumber = (int) getNextOtpNumber(login);
        logger.info("Otp №{} has been generated for client with email = {}", orderNumber, login);
        return new OtpInfo(
                otpMaxInvalidAttempts,
                otpLifetimeSeconds,
                orderNumber,
                value);
    }

    @Transactional
    public long getNextOtpNumber(String login) {
        Preconditions.checkNotNull(login, "login is null");

        LocalDateTime now = LocalDateTime.now();
        OtpCounterEntity otpCounter = otpCounterRepository.findByLogin(login);
        if (otpCounter == null) {
            otpCounter = otpCounterRepository.save(
                    new OtpCounterEntity()
                            .setLogin(login)
                            .setLastUpdated(now)
                            .setValue(1)
            );
            return otpCounter.getValue();
        }

        LocalDateTime lastUpdate = otpCounter.getLastUpdated();
        if (isTimeToReset(lastUpdate, now)) {
            otpCounter
                    .setLastUpdated(now)
                    .setValue(1);
            otpCounter = otpCounterRepository.save(otpCounter);
            return otpCounter.getValue();
        }

        long nextValue = otpCounter.getValue() + 1;
        otpCounter
                .setLastUpdated(now)
                .setValue(nextValue);
        otpCounter = otpCounterRepository.save(otpCounter);
        return otpCounter.getValue();
    }

    public static boolean isTimeToReset(LocalDateTime lastUpdate, LocalDateTime now) {
        boolean oneDayPassed = lastUpdate.until(now, ChronoUnit.DAYS) >= 1;
        boolean nextDayHasCome = lastUpdate.getDayOfYear() != now.getDayOfYear();
        return oneDayPassed || nextDayHasCome;
    }
}
