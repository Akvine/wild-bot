package ru.akvine.wild.bot.entities.security;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import ru.akvine.wild.bot.exceptions.security.NoMoreNewOtpAvailableException;
import ru.akvine.wild.bot.exceptions.security.NoMoreOtpInvalidAttemptsException;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Getter
@Setter
@Accessors(chain = true)
@Embeddable
public class OtpActionEntity {
    @Column(name = "STARTED_DATE", nullable = false)
    private LocalDateTime startedDate;

    @Column(name = "ACTION_EXPIRED_AT", nullable = false)
    private LocalDateTime actionExpiredAt;

    @Column(name = "OTP_COUNT_LEFT")
    private int otpCountLeft;

    @Nullable
    @Column(name = "OTP_NUMBER")
    private Integer otpNumber;

    @Nullable
    @Column(name = "OTP_EXPIRED_AT")
    private LocalDateTime otpExpiredAt;

    @Nullable
    @Column(name = "OTP_LAST_UPDATE")
    private LocalDateTime otpLastUpdate;

    @Column(name = "OTP_INVALID_ATTEMPTS_LEFT")
    private int otpInvalidAttemptsLeft;

    @Nullable
    @Column(name = "OTP_VALUE")
    private String otpValue;

    @Transient
    public void decrementOtpCountLeft() {
        if (this.otpCountLeft <= 0) {
            throw new NoMoreNewOtpAvailableException("No more otp codes available for generation!");
        }
        --this.otpCountLeft;
    }

    @Transient
    public int decrementInvalidAttemptsLeft() {
        if (this.otpInvalidAttemptsLeft <= 0) {
            throw new NoMoreOtpInvalidAttemptsException("No more attempts to pass otp!");
        }
        return --this.otpInvalidAttemptsLeft;
    }

    @Transient
    public boolean isOtpValid(String clientInput) {return this.otpValue.equals(clientInput);}

    @Transient
    public boolean isNewOtpLimitReached() {return this.otpCountLeft <= 0;}

    @Transient
    public boolean isActionExpired() {
        LocalDateTime expiredAt = this.actionExpiredAt;
        return expiredAt.isBefore(now());
    }

    @Transient
    public OtpActionEntity setNewOtpValue(OtpInfo newOtpInfo, LocalDateTime now) {
        setOtpValue(newOtpInfo, now);
        this.decrementOtpCountLeft();
        return this;
    }

    @Transient
    public OtpActionEntity setNewOtpValue(OtpInfo newOtpInfo) {
        this.setOtpValue(newOtpInfo, LocalDateTime.now());
        this.decrementOtpCountLeft();
        return this;
    }

    @Transient
    public void setOtpValueToNull() {
        this.setOtpNumber(null);
        this.setOtpValue(null);
        this.setOtpLastUpdate(null);
        this.setOtpExpiredAt(null);
    }

    @Transient
    private OtpActionEntity setOtpValue(OtpInfo newOtpInfo, LocalDateTime now) {
        this.setOtpNumber(newOtpInfo.getOtpNumber());
        this.setOtpValue(newOtpInfo.getOtpValue());
        this.setOtpLastUpdate(now);
        this.setOtpExpiredAt(now.plusSeconds(newOtpInfo.getOtpLifetimeSeconds()));
        return this;
    }

    @Transient
    public boolean isNotExpiredOtp() {return now().isBefore(this.otpExpiredAt);}

    @Transient
    public boolean isExpiredOtp() {return this.otpExpiredAt.isBefore(now());}
}
