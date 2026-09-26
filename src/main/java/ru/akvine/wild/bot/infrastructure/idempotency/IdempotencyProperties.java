package ru.akvine.wild.bot.infrastructure.idempotency;

import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Настройки идемпотентности (префикс {@code idempotency})
 */
@Getter
@Setter
public class IdempotencyProperties {
    private Http http = new Http();
    private Bot bot = new Bot();

    /**
     * Заголовок {@code Idempotency-Key} для изменяющих запросов к HTTP API
     */
    @Getter
    @Setter
    public static class Http {
        private boolean enabled = false;
        private String header = "Idempotency-Key";
        /** Пути (Ant-шаблоны), к которым применяется заголовок */
        private List<String> pathPatterns = List.of("/admin/**");
        /** HTTP-методы, к которым применяется заголовок */
        private Set<String> methods = Set.of("POST", "PUT", "PATCH", "DELETE");
        /** Запросы с телом больше этого размера обрабатываются без идемпотентности */
        private int maxBodyBytes = 1_048_576;
        /** Как долго ключ закреплён за выполняющимся запросом, если тот так и не завершился */
        private long inProgressTtlSeconds = 300;
        /** Сколько хранить ответ для повторных запросов */
        private long resultTtlHours = 24;
    }

    /**
     * Защита от повторной обработки одного и того же обновления бота (Telegram присылает обновление
     * повторно, если не получил ответ вовремя). Включается добавлением {@code DuplicateUpdateFilter}
     * в {@code message.filers.list}
     */
    @Getter
    @Setter
    public static class Bot {
        /** Сколько помнить обработанное обновление */
        private long ttlHours = 24;
        /** Как долго ключ закреплён за обрабатываемым обновлением, если обработка так и не завершилась */
        private long inProgressTtlSeconds = 300;
    }
}
