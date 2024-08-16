package ru.akvine.marketspace.bot.resolvers.controllers.views;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.helpers.LockHelper;
import ru.akvine.marketspace.bot.infrastructure.session.ClientSessionData;
import ru.akvine.marketspace.bot.infrastructure.session.SessionStorage;
import ru.akvine.marketspace.bot.services.AdvertService;
import ru.akvine.marketspace.bot.services.domain.AdvertModel;
import ru.akvine.marketspace.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.GetGoodsRequest;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.GetGoodsResponse;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.GoodDto;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.GoodSizeDto;
import ru.akvine.marketspace.bot.telegram.KeyboardFactory;

import static ru.akvine.marketspace.bot.constants.DbLockConstants.UPLOAD_PHOTO_LOCK;
import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.CHANGE_PRICE_BUTTON_TEXT;
import static ru.akvine.marketspace.bot.constants.telegram.TelegramButtonConstants.KEEP_PRICE_BUTTON_TEXT;

@Component
@RequiredArgsConstructor
public class IsChangePriceView implements TelegramView {
    private final WildberriesIntegrationService wildberriesIntegrationService;
    private final AdvertService advertService;
    private final LockHelper lockHelper;
    private final SessionStorage<String, ClientSessionData> sessionStorage;

    @Override
    public InlineKeyboardMarkup getKeyboard(String chatId) {
        InlineKeyboardButton changePriceButton = new InlineKeyboardButton();
        changePriceButton.setText(CHANGE_PRICE_BUTTON_TEXT);
        changePriceButton.setCallbackData(CHANGE_PRICE_BUTTON_TEXT);

        InlineKeyboardButton keepPriceButton = new InlineKeyboardButton();
        keepPriceButton.setText(KEEP_PRICE_BUTTON_TEXT);
        keepPriceButton.setCallbackData(KEEP_PRICE_BUTTON_TEXT);

        return KeyboardFactory.createVerticalKeyboard(changePriceButton, keepPriceButton, KeyboardFactory.getBackButton());
    }

    @Override
    public String getMessage(String chatId) {
        return lockHelper.doWithLock(UPLOAD_PHOTO_LOCK + chatId, () -> {
            AdvertModel advertBean = advertService.getFirst(sessionStorage.get(chatId).getSelectedCategoryId());
            advertBean.setLocked(true);
            advertService.update(advertBean);

            ClientSessionData session = sessionStorage.get(chatId);
            session.setLockedAdvertId(advertBean.getExternalId());
            sessionStorage.save(session);

            int nmId = advertBean.getItemId();
            GetGoodsRequest request = new GetGoodsRequest()
                    .setLimit(100)
                    .setFilterNmID(nmId);
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
        sb
                .append("Сейчас у карточки в рекламной кампании по выбранной категории следующая цена, скидка и скидочная цена: \n")
                .append("1. Цена без скидки: ").append(price).append("\n")
                .append("2. Скидка: ").append(discount).append("\n")
                .append("3. Цена на сайте: ").append(discountedPrice).append("\n")
                .append("Поменять цену и скидку у карточки перед запуском теста рекламной кампании?\n " +
                        "(Введите \"Изменить\" или \"Не изменять\")");
        return sb.toString();
    }
}
