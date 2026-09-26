package ru.akvine.wild.bot.infrastructure.resilience;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * Настройки bulkhead (префикс {@code bulkhead}): значения по умолчанию в {@code defaults} и
 * переопределения для отдельных внешних систем в {@code instances.<имя>}. Незаданное поле в
 * {@code instances.<имя>} берётся из {@code defaults}.
 */
@Getter
@Setter
public class BulkheadProperties {
    private boolean enabled = false;
    private Settings defaults = Settings.builtIn();
    private Map<String, Settings> instances = new HashMap<>();

    /**
     * @param name имя внешней системы, например {@code wildberries}
     * @return полные настройки bulkhead'а этой системы
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
        /** Сколько вызовов к системе может выполняться одновременно */
        private Integer maxConcurrentCalls;
        /** Сколько миллисекунд вызов ждёт свободного места, прежде чем получить отказ (0 - не ждать) */
        private Long maxWaitMillis;
        /**
         * Сколько вызовов может выполняться одновременно от имени одного клиента (по токену в заголовке
         * {@code Authorization}); 0 - без отдельного лимита на клиента. Не даёт одному клиенту занять все места
         */
        private Integer maxConcurrentCallsPerTenant;

        static Settings builtIn() {
            Settings settings = new Settings();
            settings.maxConcurrentCalls = 25;
            settings.maxWaitMillis = 1000L;
            settings.maxConcurrentCallsPerTenant = 0;
            return settings;
        }

        Settings overriddenBy(Settings override) {
            Settings result = new Settings();
            result.maxConcurrentCalls = pick(override.maxConcurrentCalls, maxConcurrentCalls);
            result.maxWaitMillis = pick(override.maxWaitMillis, maxWaitMillis);
            result.maxConcurrentCallsPerTenant =
                    pick(override.maxConcurrentCallsPerTenant, maxConcurrentCallsPerTenant);
            return result;
        }

        private static <T> T pick(T override, T base) {
            return override != null ? override : base;
        }
    }
}
