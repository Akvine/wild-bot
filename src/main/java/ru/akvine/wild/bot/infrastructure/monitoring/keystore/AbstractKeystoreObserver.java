package ru.akvine.wild.bot.infrastructure.monitoring.keystore;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractKeystoreObserver {

    protected boolean includeExpired = true;
    protected int includeExpiredDays = 30;
    protected int daysToExpire = 120;

    public AbstractKeystoreObserver() {}

    public AbstractKeystoreObserver(boolean includeExpired, int includeExpiredDays, int daysToExpire) {
        validateDaysToExpire(daysToExpire);
        validateIncludeExpiredDays(includeExpiredDays);

        this.includeExpired = includeExpired;
        this.includeExpiredDays = includeExpiredDays;
        this.daysToExpire = daysToExpire;
    }

    public abstract List<ExpireCertData> observe();

    protected int getDaysBetween(Certificate certificate) {
        LocalDate notAfter = ((X509Certificate) certificate)
                .getNotAfter()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), notAfter);
    }

    /**
     * @return {@code true}, если сертификат в отчёт попадать не должен: он уже истёк и это не нужно
     *         показывать по настройкам {@code includeExpired}/{@code includeExpiredDays}, либо
     *         истекает не скоро (позже {@code daysToExpire})
     */
    protected boolean checkForCertificateSkipping(Certificate certificate) {
        int daysBetween = getDaysBetween(certificate);

        boolean certificateExpired = daysBetween < 0;
        if (certificateExpired && (!includeExpired || (Math.abs(daysBetween) > includeExpiredDays))) {
            logger.trace(
                    "skipping certificate as already expired. includeExpired = {}, daysBetween = {}, includeExpiredDays = {}",
                    includeExpired,
                    daysBetween,
                    includeExpiredDays);
            return true;
        }

        if (daysBetween > daysToExpire) {
            logger.trace(
                    "skipping certificate as not expiring nearly. daysBetween = {}, daysToExpire = {}",
                    daysBetween,
                    daysToExpire);
            return true;
        }

        return false;
    }

    public static void validateDaysToExpire(int daysToExpire) {
        if (daysToExpire < 0) {
            throw new IllegalArgumentException("daysToExpire must be greater or equals 0");
        }
    }

    public static void validateIncludeExpiredDays(int includeExpiredDays) {
        if (includeExpiredDays < 0) {
            throw new IllegalArgumentException("includeExpiredDays must be greater or equals 0");
        }
    }
}
