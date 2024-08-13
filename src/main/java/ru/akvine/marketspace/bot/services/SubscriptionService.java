package ru.akvine.marketspace.bot.services;

import com.google.common.base.Preconditions;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.akvine.marketspace.bot.entities.ClientEntity;
import ru.akvine.marketspace.bot.entities.SubscriptionEntity;
import ru.akvine.marketspace.bot.exceptions.SubscriptionException;
import ru.akvine.marketspace.bot.repositories.ClientSubscriptionRepository;
import ru.akvine.marketspace.bot.services.domain.SubscriptionModel;
import ru.akvine.marketspace.bot.services.dto.admin.client.Subscription;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {
    private final ClientSubscriptionRepository clientSubscriptionRepository;
    private final ClientService clientService;

    @Value("${client.subscription.expires.days.after}")
    private int expiresDaysAfter;

    @Nullable
    public SubscriptionModel getByChatIdOrNull(String chatId) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        Optional<SubscriptionEntity> clientSubscription = clientSubscriptionRepository.findByChatId(chatId);
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
        return clientSubscriptionRepository
                .findByChatId(chatId)
                .orElseThrow(() -> new SubscriptionException("Client subscription for chat with id = [" + chatId + "] not found!"));
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

        SubscriptionEntity clientSubscriptionToSave = new SubscriptionEntity()
                .setClient(client)
                .setExpiresAt(LocalDateTime.now().plusDays(expiresDaysAfter));

        SubscriptionEntity savedClientSubscription = clientSubscriptionRepository.save(clientSubscriptionToSave);
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

        Optional<SubscriptionEntity> subscriptionEntity = clientSubscriptionRepository.findByChatId(chatId);
        if (subscriptionEntity.isEmpty()) {
            String errorMessage = String.format("Client with chat id = [%s] has no subscription", client.getChatId());
            throw new SubscriptionException(errorMessage);
        }

        clientSubscriptionRepository.delete(subscriptionEntity.get());
        logger.info("Subscription for client with chat id = {} has successful delete", chatId);
    }
}
