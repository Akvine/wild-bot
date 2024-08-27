package ru.akvine.wild.bot.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.akvine.wild.bot.admin.converters.SubscriptionConverter;
import ru.akvine.wild.bot.admin.dto.subscription.SubscriptionRequest;
import ru.akvine.wild.bot.admin.dto.common.Response;
import ru.akvine.wild.bot.admin.dto.common.SuccessfulResponse;
import ru.akvine.wild.bot.admin.meta.SubscriptionControllerMeta;
import ru.akvine.wild.bot.admin.validator.SubscriptionValidator;
import ru.akvine.wild.bot.services.SubscriptionService;
import ru.akvine.wild.bot.services.domain.SubscriptionModel;
import ru.akvine.wild.bot.services.dto.admin.client.Subscription;

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
