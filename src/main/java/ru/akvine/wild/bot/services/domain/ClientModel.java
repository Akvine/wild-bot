package ru.akvine.wild.bot.services.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.services.domain.base.SoftModel;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ClientModel extends SoftModel {
    private Long id;
    private String uuid;
    private String chatId;

    @Nullable
    private String username;

    private String firstName;

    @Nullable
    private String lastName;

    private int availableTestsCount;
    private boolean inWhitelist;
    private BotType botType;

    @Nullable
    private String token;

    @Nullable
    private Integer warehouseId;

    public ClientModel(ClientEntity clientEntity) {
        this.id = clientEntity.getId();
        this.uuid = clientEntity.getUuid();
        this.chatId = clientEntity.getChatId();
        this.username = clientEntity.getUsername();
        this.firstName = clientEntity.getFirstName();
        this.lastName = clientEntity.getLastName();
        this.availableTestsCount = clientEntity.getAvailableTestsCount();
        this.inWhitelist = clientEntity.isInWhitelist();
        this.botType = clientEntity.getBotType();
        this.token = clientEntity.getToken();
        this.warehouseId = clientEntity.getWarehouseId();

        this.createdDate = clientEntity.getCreatedDate();
        this.updatedDate = clientEntity.getUpdatedDate();
        this.deletedDate = clientEntity.getDeletedDate();
        this.deleted = clientEntity.isDeleted();
    }
}
