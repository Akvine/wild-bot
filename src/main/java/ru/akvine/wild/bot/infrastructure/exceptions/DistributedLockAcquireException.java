package ru.akvine.wild.bot.infrastructure.exceptions;

public class DistributedLockAcquireException extends RuntimeException {
    public DistributedLockAcquireException(String message) {
        super(message);
    }

    public DistributedLockAcquireException(String message, Exception exception) {
        super(message, exception);
    }
}
