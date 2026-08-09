package ru.akvine.wild.bot.controllers.views;

import static ru.akvine.wild.bot.constants.DbLockConstants.UPLOAD_PHOTO_LOCK;

import org.springframework.util.CollectionUtils;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;
import ru.akvine.wild.bot.infrastructure.lock.distributed.DataBaseLockProvider;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.services.AdvertService;
import ru.akvine.wild.bot.services.domain.AdvertModel;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.GetGoodsRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.GetGoodsResponse;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.GoodDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.GoodSizeDto;

@View
public class IsChangePriceView extends AbstractBotView {
    private final WildberriesIntegrationService wildberriesIntegrationService;
    private final AdvertService advertService;
    private final DataBaseLockProvider dataBaseLockProvider;
    private final SessionStorage<String, ClientSessionData> sessionStorage;

    private static final String NEW_LINE = "\n";

    public IsChangePriceView(
            BotKeyboardFactoryFacade facade,
            WildberriesIntegrationService wildberriesIntegrationService,
            AdvertService advertService,
            DataBaseLockProvider dataBaseLockProvider,
            SessionStorage<String, ClientSessionData> sessionStorage) {
        super(facade);
        this.wildberriesIntegrationService = wildberriesIntegrationService;
        this.advertService = advertService;
        this.dataBaseLockProvider = dataBaseLockProvider;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public String getMessage(String chatId) {
        return dataBaseLockProvider.doWithLock(UPLOAD_PHOTO_LOCK + chatId, () -> {
            String selectedCardType = sessionStorage.get(chatId).getSelectedCardType();
            int selectedCategoryId = sessionStorage.get(chatId).getSelectedCategoryId();
            AdvertModel advertBean = advertService.getFirst(selectedCardType, selectedCategoryId);
            advertBean.setLocked(true);
            advertService.update(advertBean);

            ClientSessionData session = sessionStorage.get(chatId);
            session.setLockedAdvertId(advertBean.getExternalId());
            sessionStorage.save(session);

            int nmId = advertBean.getCardModel().getExternalId();
            GetGoodsRequest request = new GetGoodsRequest().setLimit(100).setFilterNmID(nmId);
            GetGoodsResponse response = wildberriesIntegrationService.getGoods(request);
            if (!CollectionUtils.isEmpty(response.getData().getListGoods())) {
                GoodDto goodDto = response.getData().getListGoods().getFirst();
                GoodSizeDto size = goodDto.getSizes().getFirst();
                int discount = goodDto.getDiscount();
                int price = size.getPrice();
                double discountedPrice = size.getDiscountedPrice();
                return buildMessage(price, discount, discountedPrice);
            } else {
                String errorMessage = String.format("Card with nm id = [%s] has no goods", nmId);
                throw new IllegalStateException(errorMessage);
            }
        });
    }

    @Override
    public ClientState byState() {
        return ClientState.IS_CHANGE_PRICE_MENU;
    }

    private String buildMessage(int price, int discount, double discountedPrice) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                        "Сейчас у карточки в рекламной кампании по выбранной категории следующая цена, скидка и скидочная цена:")
                .append(NEW_LINE)
                .append("1. Цена без скидки: ")
                .append(price)
                .append(NEW_LINE)
                .append("2. Скидка: ")
                .append(discount)
                .append(NEW_LINE)
                .append("3. Цена на сайте: ")
                .append(discountedPrice)
                .append(NEW_LINE)
                .append("Поменять цену и скидку у карточки перед запуском теста рекламной кампании?")
                .append(NEW_LINE);
        return sb.toString();
    }
}
