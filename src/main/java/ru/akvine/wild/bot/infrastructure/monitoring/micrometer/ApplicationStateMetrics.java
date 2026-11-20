package ru.akvine.wild.bot.infrastructure.monitoring.micrometer;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;
import ru.akvine.wild.bot.enums.AdvertStatus;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.infrastructure.outbox.OutboxStatus;

/**
 * Показатели состояния приложения, которые берутся из базы:
 * <ul>
 *     <li>{@value #CLIENTS} - клиенты (без удалённых), метка {@code bot};</li>
 *     <li>{@value #ACTIVE_SUBSCRIPTIONS} - подписки, срок которых ещё не истёк;</li>
 *     <li>{@value #ADVERTS} - рекламные кампании (без удалённых), метка {@code status};</li>
 *     <li>{@value #OUTBOX_MESSAGES} - сообщения outbox, которые ещё не доставлены, метка {@code status}: растущее
 *     число pending означает, что доставка стоит, failed - что есть сообщения, исчерпавшие попытки.</li>
 * </ul>
 * База опрашивается, когда метрики читают, но не чаще раза в заданный интервал
 * ({@code monitoring.metrics.state.refresh.interval.milliseconds}): в промежутке отдаётся последнее значение.
 * Поэтому частый сбор метрик не нагружает базу, а пока метрики никто не собирает, запросов нет вовсе.
 */
@Slf4j
public class ApplicationStateMetrics implements MeterBinder {
    public static final String CLIENTS = "bot.clients";
    public static final String ACTIVE_SUBSCRIPTIONS = "bot.subscriptions.active";
    public static final String ADVERTS = "bot.adverts";
    public static final String OUTBOX_MESSAGES = "outbox.messages";

    private static final List<OutboxStatus> UNDELIVERED =
            List.of(OutboxStatus.PENDING, OutboxStatus.PROCESSING, OutboxStatus.FAILED);

    private static final String CLIENTS_BY_BOT =
            "select c.botType, count(c) from ClientEntity c where c.deleted = false group by c.botType";
    // local datetime - время базы в поясе её сессии, то есть то же, каким приложение записало срок подписки
    private static final String ACTIVE_SUBSCRIPTIONS_COUNT =
            "select count(s) from SubscriptionEntity s where s.expiresAt > local datetime";
    private static final String ADVERTS_BY_STATUS =
            "select a.status, count(a) from AdvertEntity a where a.deleted = false group by a.status";
    private static final String OUTBOX_MESSAGES_BY_STATUS =
            "select m.status, count(m) from OutboxMessageEntity m where m.status in :statuses group by m.status";

    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;
    private final long refreshIntervalMillis;

    private final Map<BotType, AtomicLong> clients = new EnumMap<>(BotType.class);
    private final Map<AdvertStatus, AtomicLong> adverts = new EnumMap<>(AdvertStatus.class);
    private final Map<OutboxStatus, AtomicLong> outboxMessages = new EnumMap<>(OutboxStatus.class);
    private final AtomicLong activeSubscriptions = new AtomicLong();
    private final AtomicLong nextRefreshAtMillis = new AtomicLong();

    public ApplicationStateMetrics(
            EntityManager entityManager, TransactionTemplate transactionTemplate, Duration refreshInterval) {
        this.entityManager = entityManager;
        this.transactionTemplate = transactionTemplate;
        this.refreshIntervalMillis = refreshInterval.toMillis();
        for (BotType botType : BotType.values()) {
            clients.put(botType, new AtomicLong());
        }
        for (AdvertStatus status : AdvertStatus.values()) {
            adverts.put(status, new AtomicLong());
        }
        UNDELIVERED.forEach(status -> outboxMessages.put(status, new AtomicLong()));
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        clients.forEach((botType, value) -> Gauge.builder(CLIENTS, () -> fresh(value))
                .description("Clients, deleted ones excluded")
                .tag("bot", botType.getType())
                .register(registry));
        Gauge.builder(ACTIVE_SUBSCRIPTIONS, () -> fresh(activeSubscriptions))
                .description("Subscriptions that have not expired yet")
                .register(registry);
        adverts.forEach((status, value) -> Gauge.builder(ADVERTS, () -> fresh(value))
                .description("Adverts, deleted ones excluded")
                .tag("status", lowerCase(status))
                .register(registry));
        outboxMessages.forEach((status, value) -> Gauge.builder(OUTBOX_MESSAGES, () -> fresh(value))
                .description("Outbox messages that are not delivered yet")
                .tag("status", lowerCase(status))
                .register(registry));
    }

    /**
     * Опрашивает базу сейчас, не дожидаясь, пока значения устареют. При сбое остаются прежние значения.
     */
    public void refresh() {
        try {
            transactionTemplate.executeWithoutResult(status -> read());
        } catch (RuntimeException e) {
            // метрики не должны мешать работе; следующая попытка - когда истечёт интервал
            logger.warn("Can't refresh application state metrics", e);
        }
    }

    private long fresh(AtomicLong value) {
        long now = System.currentTimeMillis();
        long refreshAt = nextRefreshAtMillis.get();
        // опрашивает базу тот, кто первым заметил, что значения устарели; остальным пока достаются прежние
        if (now >= refreshAt && nextRefreshAtMillis.compareAndSet(refreshAt, now + refreshIntervalMillis)) {
            refresh();
        }
        return value.get();
    }

    private void read() {
        update(clients, countsBy(CLIENTS_BY_BOT));
        update(adverts, countsBy(ADVERTS_BY_STATUS));
        update(
                outboxMessages,
                entityManager
                        .createQuery(OUTBOX_MESSAGES_BY_STATUS, Object[].class)
                        .setParameter("statuses", UNDELIVERED)
                        .getResultList());
        activeSubscriptions.set(entityManager
                .createQuery(ACTIVE_SUBSCRIPTIONS_COUNT, Long.class)
                .getSingleResult());
    }

    private List<Object[]> countsBy(String query) {
        return entityManager.createQuery(query, Object[].class).getResultList();
    }

    // Строки запроса - пары "значение, количество". Значения, которого в ответе нет, в базе не осталось: оно обнуляется
    private static <K extends Enum<K>> void update(Map<K, AtomicLong> gauges, List<Object[]> rows) {
        Map<Object, Long> counts = new HashMap<>();
        for (Object[] row : rows) {
            counts.put(row[0], ((Number) row[1]).longValue());
        }
        gauges.forEach((key, value) -> value.set(counts.getOrDefault(key, 0L)));
    }

    private static String lowerCase(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT);
    }
}
