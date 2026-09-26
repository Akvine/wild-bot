package ru.akvine.wild.bot.infrastructure.http.keystore;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Выбрасывает исключение или, в «тихом» режиме, только пишет предупреждение в лог
 */
@Slf4j
final class SilentUtils {

    private SilentUtils() {}

    static class ThrowOrWarner {
        private ExceptionThrower exceptionThrower;
        private ExceptionMapper exceptionMapper;
        private boolean silent = false;
        private String messageTemplateString;
        private Object[] messageTemplateParams;

        ThrowOrWarner exceptionThrower(ExceptionThrower exceptionThrower) {
            this.exceptionThrower = Objects.requireNonNull(exceptionThrower, "exceptionThrower is null");
            return this;
        }

        ThrowOrWarner exceptionMapper(ExceptionMapper exceptionMapper) {
            this.exceptionMapper = Objects.requireNonNull(exceptionMapper, "exceptionMapper is null");
            return this;
        }

        ThrowOrWarner silent(boolean silent) {
            this.silent = silent;
            return this;
        }

        ThrowOrWarner messageTemplate(String messageTemplateString, Object... messageTemplateParams) {
            this.messageTemplateString =
                    Objects.requireNonNull(messageTemplateString, "messageTemplateString is null");
            this.messageTemplateParams = messageTemplateParams;
            return this;
        }

        void throwOrWarn() {
            Objects.requireNonNull(exceptionMapper, "exceptionMapper is null");
            Objects.requireNonNull(messageTemplateString, "messageTemplateString is null");

            String resultMessage = String.format(messageTemplateString, messageTemplateParams);
            if (exceptionThrower != null) {
                try {
                    exceptionThrower.call();
                } catch (Exception localException) {
                    logger.warn(resultMessage);
                    if (!silent) {
                        throw exceptionMapper.call(resultMessage, localException);
                    }
                }
            } else {
                logger.warn(resultMessage);
                if (!silent) {
                    throw exceptionMapper.call(resultMessage, null);
                }
            }
        }
    }

    @FunctionalInterface
    interface ExceptionThrower {
        void call() throws Exception;
    }

    @FunctionalInterface
    interface ExceptionMapper {
        RuntimeException call(String message, Throwable cause);
    }
}
