package ru.akvine.wild.bot.infrastructure.session;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import ru.akvine.wild.bot.entities.infrastructure.ClientSessionDataEntity;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ClientSessionData {
    private Long id;
    @Nullable
    private String chatId;
    private String selectedCardType;
    private int selectedCategoryId;
    private byte[] uploadedCardPhoto;
    private boolean inputNewCardPriceAndDiscount;
    private Integer newCardPrice;
    private Integer newCardDiscount;
    private Integer lockedAdvertId;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public ClientSessionData(ClientSessionDataEntity clientSessionDataEntity) {
        this.id = clientSessionDataEntity.getId();
        this.chatId = clientSessionDataEntity.getChatId();
        this.selectedCardType = clientSessionDataEntity.getSelectedCardType();
        this.selectedCategoryId = clientSessionDataEntity.getSelectedCategoryId();
        this.inputNewCardPriceAndDiscount = clientSessionDataEntity.isInputNewCardPriceAndDiscount();
        this.uploadedCardPhoto = clientSessionDataEntity.getUploadedCardPhoto();
        this.newCardPrice = clientSessionDataEntity.getNewCardPrice();
        this.newCardDiscount = clientSessionDataEntity.getNewCardDiscount();
        this.lockedAdvertId = clientSessionDataEntity.getLockedAdvertId();

        this.createdDate = clientSessionDataEntity.getCreatedDate();
        this.updatedDate = clientSessionDataEntity.getUpdatedDate();
    }
}
