package ru.akvine.marketspace.bot.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import ru.akvine.marketspace.bot.entities.ClientSubscriptionEntity;
import ru.akvine.marketspace.bot.repositories.ClientSubscriptionRepository;
import ru.akvine.marketspace.bot.services.integration.telegram.TelegramIntegrationService;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class SubscriptionJob {
    private final TelegramIntegrationService telegramIntegrationService;
    private final ClientSubscriptionRepository clientSubscriptionRepository;
    private final String mdcName;
    private final String mdcChatId;

    @Value("${client.subscription.notify.days.before}")
    private int notifyDaysBefore;

    @Transactional
    @Scheduled(cron = "${delete.expired.subscriptions.cron}")
    public void deleteExpiredSubscriptions() {
        logger.info("Start delete expired subscriptions...");
        List<ClientSubscriptionEntity> subscriptions = clientSubscriptionRepository.findAll();
        subscriptions
                .stream()
                .filter(ClientSubscriptionEntity::isExpired)
                .forEach(clientSubscriptionRepository::delete);
        logger.info("End delete expired subscriptions");
    }

    @Scheduled(cron = "${notify.clients.expired.subscription.cron}")
    public void notifyClients() {
        logger.info("Start notify clients about expiring subscription...");
        List<ClientSubscriptionEntity> subscriptions = clientSubscriptionRepository.findAll();
        subscriptions
                .stream()
                .filter(subscription -> !subscription.isExpired())
                .filter(subscription -> !subscription.isNotifiedThatExpires())
                .filter(subscription -> Duration.between(ZonedDateTime.now(), subscription.getExpiresAt()).toDays() <= notifyDaysBefore)
                .forEach(subscription -> {
                    String chatId = subscription.getClient().getChatId();
                    long daysBeforeExpire = Duration.between(ZonedDateTime.now(), subscription.getExpiresAt()).toDays();
                    String message = String.format(
                            "Уважаемый пользователь! Уведомленяем вас о том, что через %s дней у вас заканчивается подписка на бота",
                            daysBeforeExpire);
                    telegramIntegrationService.sendMessage(chatId, message);
                    subscription.setNotifiedThatExpires(true);
                    clientSubscriptionRepository.save(subscription);
                });
        logger.info("End notify clients about expiring subscriptions");
    }
}
