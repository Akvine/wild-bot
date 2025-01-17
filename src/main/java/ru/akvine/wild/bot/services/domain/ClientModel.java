package ru.akvine.wild.bot.services.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.services.domain.base.SoftModel;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ClientModel extends SoftModel {
    private Long id;
    private String uuid;
    private String chatId;
    private String username;
    private String firstName;
    @Nullable
    private String lastName;
    private int availableTestsCount;
    private boolean inWhitelist;

    public ClientModel(ClientEntity clientEntity) {
        this.id = clientEntity.getId();
        this.uuid = clientEntity.getUuid();
        this.chatId = clientEntity.getChatId();
        this.username = clientEntity.getUsername();
        this.firstName = clientEntity.getFirstName();
        this.lastName = clientEntity.getLastName();
        this.availableTestsCount = clientEntity.getAvailableTestsCount();
        this.inWhitelist = clientEntity.isInWhitelist();

        this.createdDate = clientEntity.getCreatedDate();
        this.updatedDate = clientEntity.getUpdatedDate();
        this.deletedDate = clientEntity.getDeletedDate();
        this.deleted = clientEntity.isDeleted();
    }
}
