package ru.akvine.marketspace.bot.services.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import ru.akvine.marketspace.bot.entities.ClientEntity;
import ru.akvine.marketspace.bot.services.domain.base.SoftBean;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ClientBean extends SoftBean {
    private Long id;
    private String uuid;
    private String chatId;
    private String username;
    private String firstName;
    @Nullable
    private String lastName;

    public ClientBean(ClientEntity clientEntity) {
        this.id = clientEntity.getId();
        this.uuid = clientEntity.getUuid();
        this.chatId = clientEntity.getChatId();
        this.username = clientEntity.getUsername();
        this.firstName = clientEntity.getFirstName();
        this.lastName = clientEntity.getLastName();

        this.createdDate = clientEntity.getCreatedDate();
        this.updatedDate = clientEntity.getUpdatedDate();
        this.deletedDate = clientEntity.getDeletedDate();
        this.deleted = clientEntity.isDeleted();
    }
}
