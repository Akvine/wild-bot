package ru.akvine.wild.bot.controllers.views;

import static ru.akvine.wild.bot.constants.LockConstants.UPLOAD_PHOTO_LOCK;

import org.springframework.util.CollectionUtils;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;
import ru.akvine.wild.bot.infrastructure.lock.DistributedLockProvider;
import ru.akvine.wild.bot.infrastructure.session.ClientSessionData;
import ru.akvine.wild.bot.infrastructure.session.SessionStorage;
import ru.akvine.wild.bot.services.AdvertService;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.domain.AdvertModel;
import ru.akvine.wild.bot.services.domain.ClientModel;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.GetGoodsRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.GetGoodsResponse;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.GoodDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.GoodSizeDto;

@View
public class IsChangePriceView extends AbstractBotView {
    private final WildberriesIntegrationService wildberriesIntegrationService;
    private final AdvertService advertService;
    private final DistributedLockProvider lockProvider;
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final ClientService clientService;

    private static final String NEW_LINE = "\n";

    public IsChangePriceView(
            BotKeyboardFactoryFacade facade,
            WildberriesIntegrationService wildberriesIntegrationService,
            AdvertService advertService,
            DistributedLockProvider lockProvider,
            SessionStorage<String, ClientSessionData> sessionStorage,
            ClientService clientService) {
        super(facade);
        this.wildberriesIntegrationService = wildberriesIntegrationService;
        this.advertService = advertService;
        this.lockProvider = lockProvider;
        this.sessionStorage = sessionStorage;
        this.clientService = clientService;
    }

    @Override
    public String getMessage(String chatId, BotType botType) {
        return lockProvider.lock(UPLOAD_PHOTO_LOCK + chatId, () -> {
            String selectedCardType = sessionStorage.get(chatId, botType).getSelectedCardType();
            int selectedCategoryId = sessionStorage.get(chatId, botType).getSelectedCategoryId();

            ClientModel client = clientService.getByChatIdAndBotType(chatId, botType);
            AdvertModel advertBean = advertService.getFirst(selectedCardType, selectedCategoryId, client);

            ClientSessionData session = sessionStorage.get(chatId, botType);
            session.setAdvertIdToStart(advertBean.getExternalId());
            sessionStorage.save(session, botType);

            int nmId = advertBean.getCardModel().getExternalId();
            GetGoodsRequest request = new GetGoodsRequest().setLimit(100).setFilterNmID(nmId);
            GetGoodsResponse response = wildberriesIntegrationService.getGoods(request, client.getToken());
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
