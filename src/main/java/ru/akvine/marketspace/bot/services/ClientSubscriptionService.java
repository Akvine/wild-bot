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
import ru.akvine.marketspace.bot.entities.ClientSubscriptionEntity;
import ru.akvine.marketspace.bot.exceptions.ClientSubscriptionException;
import ru.akvine.marketspace.bot.repositories.ClientSubscriptionRepository;
import ru.akvine.marketspace.bot.services.domain.ClientSubscriptionBean;
import ru.akvine.marketspace.bot.services.dto.admin.client.Subscription;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientSubscriptionService {
    private final ClientSubscriptionRepository clientSubscriptionRepository;
    private final ClientService clientService;

    @Value("${client.subscription.expires.days.after}")
    private int expiresDaysAfter;

    @Nullable
    public ClientSubscriptionBean getByChatIdOrNull(String chatId) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        Optional<ClientSubscriptionEntity> clientSubscription = clientSubscriptionRepository.findByChatId(chatId);
        return clientSubscription.map(ClientSubscriptionBean::new).orElse(null);
    }

    public ClientSubscriptionBean add(Subscription subscription) {
        Preconditions.checkNotNull(subscription, "subscription is null");
        logger.info("Add subscription by [{}]", subscription);

        ClientEntity client;
        if (StringUtils.isNotBlank(subscription.getUsername())) {
            client = clientService.verifyExistsByUsername(subscription.getUsername());
        } else {
            client = clientService.verifyExistsByChatId(subscription.getChatId());
        }

        ClientSubscriptionEntity clientSubscriptionToSave = new ClientSubscriptionEntity()
                .setClient(client)
                .setExpiresAt(LocalDateTime.now().plusDays(expiresDaysAfter));

        ClientSubscriptionEntity savedClientSubscription = clientSubscriptionRepository.save(clientSubscriptionToSave);
        logger.info("Successful add subscription = [{}] to client with chat id = {}", savedClientSubscription, client.getChatId());
        return new ClientSubscriptionBean(savedClientSubscription);
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

        Optional<ClientSubscriptionEntity> subscriptionEntity = clientSubscriptionRepository.findByChatId(chatId);
        if (subscriptionEntity.isEmpty()) {
            String errorMessage = String.format("Client with chat id = [%s] has no subscription", client.getChatId());
            throw new ClientSubscriptionException(errorMessage);
        }

        clientSubscriptionRepository.delete(subscriptionEntity.get());
        logger.info("Subscription for client with chat id = {} has successful delete", chatId);
    }
}
