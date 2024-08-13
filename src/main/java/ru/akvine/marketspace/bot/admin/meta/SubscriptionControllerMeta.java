package ru.akvine.marketspace.bot.admin.meta;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.akvine.marketspace.bot.admin.dto.subscription.SubscriptionRequest;
import ru.akvine.marketspace.bot.admin.dto.common.Response;

@RequestMapping(value = "/admin/subscriptions")
public interface SubscriptionControllerMeta {
    @PostMapping(value = "/get")
    Response get(@Valid @RequestBody SubscriptionRequest request);

    @PostMapping(value = "/add")
    Response addSubscription(@Valid @RequestBody SubscriptionRequest request);

    @PostMapping(value = "/delete")
    Response deleteSubscription(@Valid @RequestBody SubscriptionRequest request);
}
