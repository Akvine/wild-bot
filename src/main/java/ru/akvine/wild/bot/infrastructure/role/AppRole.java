package ru.akvine.wild.bot.infrastructure.role;

import java.util.Arrays;
import java.util.Locale;
import org.springframework.util.StringUtils;

/**
 * Роль процесса ({@code server.app.role}): одна и та же сборка запускается как единое приложение или как два отдельных
 * процесса - приём сообщений и фоновая работа.
 * <ul>
 *     <li>{@link #ALL} (по умолчанию) - всё в одном процессе, как раньше;</li>
 *     <li>{@link #WEB} - вебхуки Telegram/Max, админский REST, диспетчер сообщений; фоновых джобов нет;</li>
 *     <li>{@link #WORKER} - фоновые джобы (синхронизация с Wildberries, проверка кампаний, подписки, очистка) и
 *     доставка сообщений из outbox; вебхук и команды в Telegram не регистрируются.</li>
 * </ul>
 * Служебные задачи самого процесса (мониторинг, обновление кеша блокировок, синхронизация настроек из Custodian)
 * работают в любой роли.
 */
public enum AppRole {
    ALL,
    WEB,
    WORKER;

    public static final String PROPERTY = "server.app.role";
    public static final String DEFAULT_VALUE = "all";

    /**
     * Разбирает значение настройки. Пустое значение - {@link #ALL}, опечатка - ошибка при старте, а не тихий запуск
     * в неожиданной роли.
     */
    public static AppRole of(String value) {
        if (!StringUtils.hasText(value)) {
            return ALL;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(role -> role.name().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown " + PROPERTY + " = '" + value + "', allowed values: all, web, worker"));
    }

    /** Запускает ли процесс фоновые джобы и доставку outbox */
    public boolean runsJobs() {
        return this != WEB;
    }

    /** Принимает ли процесс входящий трафик (вебхуки, админский REST), а значит отвечает за регистрацию вебхука */
    public boolean servesTraffic() {
        return this != WORKER;
    }
}
