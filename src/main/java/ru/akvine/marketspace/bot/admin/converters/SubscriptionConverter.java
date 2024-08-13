package ru.akvine.marketspace.bot.admin.converters;

import com.google.common.base.Preconditions;
import org.springframework.stereotype.Component;
import ru.akvine.marketspace.bot.admin.dto.client.SubscriptionRequest;
import ru.akvine.marketspace.bot.admin.dto.subscription.SubscriptionResponse;
import ru.akvine.marketspace.bot.services.domain.SubscriptionModel;
import ru.akvine.marketspace.bot.services.dto.admin.client.Subscription;

@Component
public class SubscriptionConverter {
    public Subscription convertToSubscription(SubscriptionRequest request) {
        Preconditions.checkNotNull(request, "SubscriptionRequest is null");
        return new Subscription()
                .setChatId(request.getChatId())
                .setUsername(request.getUsername());
    }

    public SubscriptionResponse convertToSubscriptionResponse(SubscriptionModel subscriptionModel) {
        return new SubscriptionResponse()
                .setChatId(subscriptionModel.getClientBean().getChatId())
                .setExpiresAt(subscriptionModel.getExpiresAt())
                .setNotifiedThatExpires(subscriptionModel.isNotifiedThatExpires())
                .setExpires(subscriptionModel.isExpired());
    }
}
