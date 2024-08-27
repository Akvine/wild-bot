package ru.akvine.wild.bot.services.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.wild.bot.entities.SubscriptionEntity;
import ru.akvine.wild.bot.services.domain.base.Model;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class SubscriptionModel extends Model {
    private Long id;
    private LocalDateTime expiresAt;
    private boolean notifiedThatExpires;
    private ClientModel clientBean;

    public SubscriptionModel(SubscriptionEntity subscriptionEntity) {
        this.id = subscriptionEntity.getId();
        this.expiresAt = subscriptionEntity.getExpiresAt();
        this.notifiedThatExpires = subscriptionEntity.isNotifiedThatExpires();
        this.clientBean = new ClientModel(subscriptionEntity.getClient());

        this.createdDate = subscriptionEntity.getCreatedDate();
        this.updatedDate = subscriptionEntity.getUpdatedDate();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
