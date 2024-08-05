package ru.akvine.marketspace.bot.entities.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.akvine.marketspace.bot.entities.base.BaseEntity;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Table(name = "CLIENT_SESSION_DATA_ENTITY")
@Entity
public class ClientSessionDataEntity extends BaseEntity {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clientSessionDataEntitySeq")
    @SequenceGenerator(name = "clientSessionDataEntitySeq", sequenceName = "SEQ_CLIENT_SESSION_DATA_ENTITY", allocationSize = 1000)
    private Long id;

    @Column(name = "CHAT_ID", nullable = false)
    private String chatId;

    @Column(name = "SELECTED_CATEGORY_ID", nullable = false)
    private int selectedCategoryId;

    @Column(name = "UPLOADED_CARD_PHOTO")
    private byte[] uploadedCardPhoto;

    @Column(name = "IS_INPUT_NEW_CARD_PRICE_AND_DISCOUNT", nullable = false)
    private boolean inputNewCardPriceAndDiscount;

    @Column(name = "NEW_CARD_PRICE")
    private Integer newCardPrice;

    @Column(name = "NEW_CARD_DISCOUNT")
    private Integer newCardDiscount;

    @Column(name = "LOCKED_ADVERT_ID")
    private Integer lockedAdvertId;
}
