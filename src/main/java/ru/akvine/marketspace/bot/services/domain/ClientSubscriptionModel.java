package ru.akvine.marketspace.bot.services.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.entities.ClientSubscriptionEntity;
import ru.akvine.marketspace.bot.services.domain.base.Model;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class ClientSubscriptionModel extends Model {
    private Long id;
    private LocalDateTime expiresAt;
    private boolean notifiedThatExpires;
    private ClientModel clientBean;

    public ClientSubscriptionModel(ClientSubscriptionEntity clientSubscriptionEntity) {
        this.id = clientSubscriptionEntity.getId();
        this.expiresAt = clientSubscriptionEntity.getExpiresAt();
        this.notifiedThatExpires = clientSubscriptionEntity.isNotifiedThatExpires();
        this.clientBean = new ClientModel(clientSubscriptionEntity.getClient());

        this.createdDate = clientSubscriptionEntity.getCreatedDate();
        this.updatedDate = clientSubscriptionEntity.getUpdatedDate();
    }

    public boolean isExpiresAt() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
