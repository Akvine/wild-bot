package ru.akvine.wild.bot.admin.meta;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.akvine.wild.bot.admin.dto.common.Response;
import ru.akvine.wild.bot.admin.dto.subscription.SubscriptionRequest;

/**
 * Контракт админ-панели для управления подписками клиентов.
 */
@RequestMapping(value = "/admin/subscriptions")
public interface SubscriptionControllerMeta {
    /**
     * Возвращает подписку клиента.
     *
     * @param request идентификатор клиента
     * @return подписка клиента
     */
    @PostMapping(value = "/get")
    Response get(@Valid @RequestBody SubscriptionRequest request);

    /**
     * Оформляет клиенту подписку.
     *
     * @param request идентификатор клиента и параметры подписки
     * @return успешный ответ
     */
    @PostMapping(value = "/add")
    Response addSubscription(@Valid @RequestBody SubscriptionRequest request);

    /**
     * Удаляет подписку клиента.
     *
     * @param request идентификатор клиента
     * @return успешный ответ
     */
    @PostMapping(value = "/delete")
    Response deleteSubscription(@Valid @RequestBody SubscriptionRequest request);
}
