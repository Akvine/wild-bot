package ru.akvine.wild.bot.infrastructure.httplogging;

import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/**
 * Копит начало тела запроса по мере его вычитывания и отдаёт подписчику, когда чтение закончено
 */
@Slf4j
public class HttpRequestLoggingContext {
    private final ByteArrayOutputStream bodyByteArrayOutputStream = new ByteArrayOutputStream();
    private boolean bodyWasLogged = false;
    private boolean readWasFinished = false;
    private boolean subscribed = false;
    private Consumer<byte[]> bodyReadCallback = bytes -> bodyWasLogged = true;

    HttpRequestLoggingContext() {
        logger.trace("new instance of logging context was created");
    }

    void subscribeForBody(Consumer<byte[]> consumer) {
        logger.debug("subscribeForBody method called");
        if (bodyWasLogged || subscribed) {
            logger.error(
                    "body was already subscribed. You are doing something wrong. bodyWasLogged = [{}], subscribed = [{}]",
                    bodyWasLogged,
                    subscribed);
            return;
        }

        bodyReadCallback = consumer.andThen(bodyReadCallback);
        subscribed = true;

        if (readWasFinished) {
            logger.trace("running callback because read from body was already finished");
            bodyReadCallback.accept(bodyByteArrayOutputStream.toByteArray());
        }
    }

    public boolean wasBodyLogged() {
        return bodyWasLogged;
    }

    public void readFinished() {
        logger.trace("readFinished method called");
        if (subscribed && !this.readWasFinished) {
            logger.trace("running callback, because somewho already subscribed for body");
            this.readWasFinished = true;
            bodyReadCallback.accept(bodyByteArrayOutputStream.toByteArray());
        }
        this.readWasFinished = true;
    }

    public int getCachedBodySize() {
        return bodyByteArrayOutputStream.size();
    }

    void writeToBodyCache(byte[] source, int sourceOffset, int len) {
        bodyByteArrayOutputStream.write(source, sourceOffset, len);
    }
}
