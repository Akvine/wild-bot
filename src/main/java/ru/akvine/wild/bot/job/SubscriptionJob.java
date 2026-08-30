package ru.akvine.wild.bot.job;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import ru.akvine.wild.bot.constants.MDCConstants;
import ru.akvine.wild.bot.entities.SubscriptionEntity;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.repositories.SubscriptionRepository;
import ru.akvine.wild.bot.services.integration.BotIntegrationAdapter;

@RequiredArgsConstructor
@Slf4j
public class SubscriptionJob {
    private final BotIntegrationAdapter botIntegrationAdapter;
    private final SubscriptionRepository subscriptionRepository;
    private final String mdcName;
    private final String mdcChatId;

    @Value("${client.subscription.notify.days.before}")
    private int notifyDaysBefore;

    @Transactional
    @Scheduled(cron = "${delete.expired.subscriptions.cron}")
    public void deleteExpiredSubscriptions() {
        MDC.put(MDCConstants.USERNAME, mdcName);
        MDC.put(MDCConstants.CHAT_ID, mdcChatId);
        logger.info("Start delete expired subscriptions...");
        List<SubscriptionEntity> subscriptions = subscriptionRepository.findAll();
        subscriptions.stream().filter(SubscriptionEntity::isExpired).forEach(subscriptionRepository::delete);
        logger.info("End delete expired subscriptions");
    }

    @Scheduled(cron = "${notify.clients.expired.subscription.cron}")
    public void notifyClients() {
        MDC.put(MDCConstants.USERNAME, mdcName);
        MDC.put(MDCConstants.CHAT_ID, mdcChatId);
        logger.info("Start notify clients about expiring subscription...");
        List<SubscriptionEntity> subscriptions = subscriptionRepository.findAll();
        subscriptions.stream()
                .filter(subscription -> !subscription.isExpired())
                .filter(subscription -> !subscription.isNotifiedThatExpires())
                .filter(subscription -> Duration.between(LocalDateTime.now(), subscription.getExpiresAt())
                                .toDays()
                        <= notifyDaysBefore)
                .forEach(subscription -> {
                    String chatId = subscription.getClient().getChatId();
                    BotType botType = subscription.getClient().getBotType();
                    long daysBeforeExpire = Duration.between(LocalDateTime.now(), subscription.getExpiresAt())
                            .toDays();
                    String message = String.format(
                            "Уважаемый пользователь! Уведомляем вас о том, что через %s дня у вас заканчивается подписка на бота",
                            daysBeforeExpire);
                    botIntegrationAdapter.sendMessage(chatId, botType, message);
                    subscription.setNotifiedThatExpires(true);
                    subscriptionRepository.save(subscription);
                });
        logger.info("End notify clients about expiring subscription");
    }
}
