package ru.akvine.wild.bot.infrastructure.resilience;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * Настройки circuit breaker (префикс {@code circuit.breaker}): значения по умолчанию в {@code defaults}
 * и переопределения для отдельных внешних систем в {@code instances.<имя>}. Незаданное поле
 * в {@code instances.<имя>} берётся из {@code defaults}.
 */
@Getter
@Setter
public class CircuitBreakerProperties {
    private boolean enabled = false;
    private Settings defaults = Settings.builtIn();
    private Map<String, Settings> instances = new HashMap<>();

    /**
     * @param name имя внешней системы, например {@code wildberries}
     * @return полные настройки breaker'а этой системы
     */
    public Settings resolve(String name) {
        Settings override = instances.get(name);
        return override == null ? defaults : defaults.overriddenBy(override);
    }

    /**
     * Все поля необязательные: пустое поле в {@code instances.<имя>} означает «как в defaults»
     */
    @Getter
    @Setter
    public static class Settings {
        /** Сколько последних вызовов учитывается при расчёте доли ошибок */
        private Integer slidingWindowSize;
        /** Сколько вызовов должно накопиться, прежде чем breaker может открыться */
        private Integer minimumNumberOfCalls;
        /** Доля ошибок в окне, %, при которой breaker открывается */
        private Float failureRateThreshold;
        /** Сколько секунд breaker остаётся открытым, отклоняя вызовы, прежде чем пропустить пробные */
        private Integer waitDurationInOpenStateSeconds;
        /** Сколько пробных вызовов пропускается в полуоткрытом состоянии */
        private Integer permittedCallsInHalfOpenState;
        /** Вызов дольше этого числа секунд считается медленным */
        private Integer slowCallDurationThresholdSeconds;
        /** Доля медленных вызовов, %, при которой breaker открывается (100 - не учитывать медленные) */
        private Float slowCallRateThreshold;

        static Settings builtIn() {
            Settings settings = new Settings();
            settings.slidingWindowSize = 20;
            settings.minimumNumberOfCalls = 10;
            settings.failureRateThreshold = 50f;
            settings.waitDurationInOpenStateSeconds = 30;
            settings.permittedCallsInHalfOpenState = 3;
            settings.slowCallDurationThresholdSeconds = 60;
            settings.slowCallRateThreshold = 100f;
            return settings;
        }

        Settings overriddenBy(Settings override) {
            Settings result = new Settings();
            result.slidingWindowSize = pick(override.slidingWindowSize, slidingWindowSize);
            result.minimumNumberOfCalls = pick(override.minimumNumberOfCalls, minimumNumberOfCalls);
            result.failureRateThreshold = pick(override.failureRateThreshold, failureRateThreshold);
            result.waitDurationInOpenStateSeconds =
                    pick(override.waitDurationInOpenStateSeconds, waitDurationInOpenStateSeconds);
            result.permittedCallsInHalfOpenState =
                    pick(override.permittedCallsInHalfOpenState, permittedCallsInHalfOpenState);
            result.slowCallDurationThresholdSeconds =
                    pick(override.slowCallDurationThresholdSeconds, slowCallDurationThresholdSeconds);
            result.slowCallRateThreshold = pick(override.slowCallRateThreshold, slowCallRateThreshold);
            return result;
        }

        private static <T> T pick(T override, T base) {
            return override != null ? override : base;
        }
    }
}
