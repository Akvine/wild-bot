package ru.akvine.wild.bot.infrastructure.session;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import ru.akvine.wild.bot.entities.infrastructure.ClientSessionDataEntity;
import ru.akvine.wild.bot.enums.BotType;

/**
 * Данные текущего диалога клиента с ботом: выбранный тип/категория карточки, загруженное фото,
 * вводимые новые цена/скидка, id заблокированной под тест кампании. Хранится и читается через
 * {@link SessionStorage}; сбрасывается при завершении сценария или команде {@code /start}.
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ClientSessionData {
    private Long id;
    private BotType botType;

    @Nullable
    private String chatId;

    private String selectedCardType;
    private int selectedCategoryId;
    private byte[] uploadedCardPhoto;
    private boolean inputNewCardPriceAndDiscount;
    private Integer newCardPrice;
    private Integer newCardDiscount;
    private Integer advertIdToStart;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public ClientSessionData(ClientSessionDataEntity clientSessionDataEntity) {
        this.id = clientSessionDataEntity.getId();
        this.chatId = clientSessionDataEntity.getChatId();
        this.botType = clientSessionDataEntity.getBotType();
        this.selectedCardType = clientSessionDataEntity.getSelectedCardType();
        this.selectedCategoryId = clientSessionDataEntity.getSelectedCategoryId();
        this.inputNewCardPriceAndDiscount = clientSessionDataEntity.isInputNewCardPriceAndDiscount();
        this.uploadedCardPhoto = clientSessionDataEntity.getUploadedCardPhoto();
        this.newCardPrice = clientSessionDataEntity.getNewCardPrice();
        this.newCardDiscount = clientSessionDataEntity.getNewCardDiscount();
        this.advertIdToStart = clientSessionDataEntity.getAdvertIdToStart();

        this.createdDate = clientSessionDataEntity.getCreatedDate();
        this.updatedDate = clientSessionDataEntity.getUpdatedDate();
    }
}
