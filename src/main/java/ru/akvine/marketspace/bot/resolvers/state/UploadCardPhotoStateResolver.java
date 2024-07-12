package ru.akvine.marketspace.bot.resolvers.state;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.infrastructure.SessionStorage;
import ru.akvine.marketspace.bot.infrastructure.StateStorage;
import ru.akvine.marketspace.bot.infrastructure.impl.ClientSessionData;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.services.AdvertService;
import ru.akvine.marketspace.bot.services.domain.AdvertBean;
import ru.akvine.marketspace.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.marketspace.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.GetGoodsRequest;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.GetGoodsResponse;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.GoodDto;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.GoodSizeDto;
import ru.akvine.marketspace.bot.telegram.KeyboardFactory;
import ru.akvine.marketspace.bot.telegram.TelegramData;
import ru.akvine.marketspace.bot.utils.TelegramPhotoResolver;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadCardPhotoStateResolver implements StateResolver {
    private final TelegramDataResolverManager dataResolverManager;
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final StateStorage<String> stateStorage;
    private final TelegramPhotoResolver telegramPhotoResolver;
    private final TelegramIntegrationService telegramIntegrationService;
    private final WildberriesIntegrationService wildberriesIntegrationService;
    private final AdvertService advertService;

    @Override
    public BotApiMethod<?> resolve(TelegramData data) {
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(data.getType());
        String chatId = resolver.extractChatId(data.getData());

        if (data.getData().getMessage().getPhoto() == null) {
            return new SendMessage(chatId, "Загрузите фотографию карточки для теста РК или\n" +
                    "введите команду /cancel для отмены запуска");
        }

        PhotoSize photoSize = telegramPhotoResolver.resolve(data.getData().getMessage().getPhoto());
        byte[] photo = telegramIntegrationService.downloadPhoto(photoSize.getFileId(), chatId);

        // TODO : тут надо поставить лок
        AdvertBean advertBean = advertService.getFirst(sessionStorage.get(chatId).getChoosenCategoryId());
        advertBean.setLocked(true);
        advertService.update(advertBean);
        sessionStorage.get(chatId).setLockedAdvertId(advertBean.getAdvertId());

        int nmId = Integer.parseInt(advertBean.getItemId());
        GetGoodsRequest request = new GetGoodsRequest()
                .setLimit(100)
                .setFilterNmID(nmId);
        GetGoodsResponse response = wildberriesIntegrationService.getGoods(request);
        String message;
        if (!CollectionUtils.isEmpty(response.getData().getListGoods())) {
            GoodDto goodDto = response.getData().getListGoods().getFirst();
            GoodSizeDto size = goodDto.getSizes().getFirst();
            int discount = goodDto.getDiscount();
            int price = size.getPrice();
            double discountedPrice=  size.getDiscountedPrice();
            message = buildMessage(price, discount, discountedPrice);
        } else {
            String errorMessage = String.format("Card with nm id = [%s] has no goods", nmId);
            throw new IllegalStateException(errorMessage);
        }
        //

        sessionStorage.get(chatId).setUploadedCardPhoto(photo);
        stateStorage.setState(chatId, ClientState.IS_NEED_INPUT_NEW_CARD_PRICE_STATE);

        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(KeyboardFactory.getYesAndNoKeyboard());
        return sendMessage;
    }

    @Override
    public ClientState getState() {
        return ClientState.UPLOAD_NEW_CARD_PHOTO_STATE;
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
