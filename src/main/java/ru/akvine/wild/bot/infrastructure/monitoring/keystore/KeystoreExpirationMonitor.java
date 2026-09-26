package ru.akvine.wild.bot.infrastructure.monitoring.keystore;

import static ru.akvine.commons.util.ScheduledExecutors.newSingleThreadScheduledExecutor;
import static ru.akvine.commons.util.Threads.newThreadFactory;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Периодически просматривает ключницы и пишет в лог сертификаты, срок действия которых истекает
 * (WARN) или уже истёк (ERROR). Первая проверка - сразу при старте
 */
@Slf4j
public class KeystoreExpirationMonitor {
    private final KeyStoreObserver keyStoreObserver;
    private final long intervalHours;
    private final int daysToExpire;
    private final ScheduledExecutorService executor;

    public KeystoreExpirationMonitor(KeyStoreObserver keyStoreObserver, long intervalHours, int daysToExpire) {
        if (intervalHours < 1) {
            throw new IllegalArgumentException("intervalHours must be at least 1 but was " + intervalHours);
        }
        this.keyStoreObserver = keyStoreObserver;
        this.intervalHours = intervalHours;
        this.daysToExpire = daysToExpire;
        this.executor = newSingleThreadScheduledExecutor(newThreadFactory("keystore-expiration-monitor"));
    }

    public void start() {
        executor.scheduleWithFixedDelay(this::check, 0, intervalHours, TimeUnit.HOURS);
    }

    public void stop() {
        executor.shutdownNow();
    }

    public void check() {
        try {
            List<ExpireCertData> certificates = keyStoreObserver.observe();
            if (certificates.isEmpty()) {
                logger.info("KEYSTORE_CHECK: no certificates expire within {} days", daysToExpire);
                return;
            }
            certificates.forEach(this::log);
        } catch (Exception e) {
            logger.error("Failed to check keystore certificates", e);
        }
    }

    private void log(ExpireCertData cert) {
        if (cert.expiryIn() < 0) {
            logger.error(
                    "KEYSTORE_CHECK: certificate [{}] from [{}] expired {} days ago, sha256: {}",
                    cert.alias(),
                    cert.source(),
                    -cert.expiryIn(),
                    cert.hash());
        } else {
            logger.warn(
                    "KEYSTORE_CHECK: certificate [{}] from [{}] expires in {} days, sha256: {}",
                    cert.alias(),
                    cert.source(),
                    cert.expiryIn(),
                    cert.hash());
        }
    }
}
