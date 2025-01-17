package ru.akvine.wild.bot.services;

import com.google.common.base.Preconditions;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.entities.SubscriptionEntity;
import ru.akvine.wild.bot.exceptions.HasNoSubscriptionException;
import ru.akvine.wild.bot.repositories.SubscriptionRepository;
import ru.akvine.wild.bot.services.domain.SubscriptionModel;
import ru.akvine.wild.bot.services.dto.admin.client.Subscription;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final ClientService clientService;

    @Value("${client.subscription.expires.days.after}")
    private int expiresDaysAfter;

    @Nullable
    public SubscriptionModel getByChatIdOrNull(String chatId) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        Optional<SubscriptionEntity> clientSubscription = subscriptionRepository.findByChatId(chatId);
        return clientSubscription.map(SubscriptionModel::new).orElse(null);
    }

    public SubscriptionModel get(Subscription subscription) {
        Preconditions.checkNotNull(subscription, "subscription is null");
        logger.info("Get subscription by [{}]", subscription);

        ClientEntity client;
        if (StringUtils.isNotBlank(subscription.getUsername())) {
            client = clientService.verifyExistsByUsername(subscription.getUsername());
        } else {
            client = clientService.verifyExistsByChatId(subscription.getChatId());
        }

        return new SubscriptionModel(verifyExistsByChatId(client.getChatId()));
    }

    public SubscriptionEntity verifyExistsByChatId(String chatId) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        return subscriptionRepository
                .findByChatId(chatId)
                .orElseThrow(() -> new HasNoSubscriptionException("Client subscription for chat with id = [" + chatId + "] not found!"));
    }

    public SubscriptionModel add(Subscription subscription) {
        Preconditions.checkNotNull(subscription, "subscription is null");
        logger.info("Add subscription by [{}]", subscription);

        ClientEntity client;
        if (StringUtils.isNotBlank(subscription.getUsername())) {
            client = clientService.verifyExistsByUsername(subscription.getUsername());
        } else {
            client = clientService.verifyExistsByChatId(subscription.getChatId());
        }

        SubscriptionEntity subscriptionEntity;
        try {
            subscriptionEntity = verifyExistsByChatId(client.getChatId());
        } catch (HasNoSubscriptionException exception) {
            subscriptionEntity = new SubscriptionEntity();
        }

        subscriptionEntity
                .setClient(client)
                .setExpiresAt(LocalDateTime.now().plusDays(expiresDaysAfter));


        SubscriptionEntity savedClientSubscription = subscriptionRepository.save(subscriptionEntity);
        logger.info("Successful add subscription = [{}] to client with chat id = {}", savedClientSubscription, client.getChatId());
        return new SubscriptionModel(savedClientSubscription);
    }

    @Transactional
    public void delete(Subscription subscription) {
        Preconditions.checkNotNull(subscription, "subscription is null");
        logger.info("Delete subscription by [{}]", subscription);

        ClientEntity client;
        if (StringUtils.isNotBlank(subscription.getUsername())) {
            client = clientService.verifyExistsByUsername(subscription.getUsername());
        } else {
            client = clientService.verifyExistsByChatId(subscription.getChatId());
        }
        String chatId = client.getChatId();

        Optional<SubscriptionEntity> subscriptionEntity = subscriptionRepository.findByChatId(chatId);
        if (subscriptionEntity.isEmpty()) {
            String errorMessage = String.format("Client with chat id = [%s] has no subscription", client.getChatId());
            throw new HasNoSubscriptionException(errorMessage);
        }

        subscriptionRepository.delete(subscriptionEntity.get());
        logger.info("Subscription for client with chat id = {} has successful delete", chatId);
    }
}
