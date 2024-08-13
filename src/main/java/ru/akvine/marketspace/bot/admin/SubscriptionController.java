package ru.akvine.marketspace.bot.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.akvine.marketspace.bot.admin.converters.SubscriptionConverter;
import ru.akvine.marketspace.bot.admin.dto.subscription.SubscriptionRequest;
import ru.akvine.marketspace.bot.admin.dto.common.Response;
import ru.akvine.marketspace.bot.admin.dto.common.SuccessfulResponse;
import ru.akvine.marketspace.bot.admin.meta.SubscriptionControllerMeta;
import ru.akvine.marketspace.bot.admin.validator.SubscriptionValidator;
import ru.akvine.marketspace.bot.services.SubscriptionService;
import ru.akvine.marketspace.bot.services.domain.SubscriptionModel;
import ru.akvine.marketspace.bot.services.dto.admin.client.Subscription;

@RestController
@RequiredArgsConstructor
public class SubscriptionController implements SubscriptionControllerMeta {
    private final SubscriptionService subscriptionService;
    private final SubscriptionValidator subscriptionValidator;
    private final SubscriptionConverter subscriptionConverter;

    @Override
    public Response get(SubscriptionRequest request) {
        subscriptionValidator.verifySubscriptionRequest(request);
        Subscription subscription = subscriptionConverter.convertToSubscription(request);
        SubscriptionModel getSubscription = subscriptionService.get(subscription);
        return subscriptionConverter.convertToSubscriptionResponse(getSubscription);
    }

    @Override
    public Response addSubscription(SubscriptionRequest request) {
        subscriptionValidator.verifySubscriptionRequest(request);
        Subscription subscription = subscriptionConverter.convertToSubscription(request);
        subscriptionService.add(subscription);
        return new SuccessfulResponse();
    }

    @Override
    public Response deleteSubscription(SubscriptionRequest request) {
        subscriptionValidator.verifySubscriptionRequest(request);
        Subscription subscription = subscriptionConverter.convertToSubscription(request);
        subscriptionService.delete(subscription);
        return new SuccessfulResponse();
    }
}
