package ru.akvine.wild.bot.controllers.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.entities.AdvertStatisticEntity;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.exceptions.AdvertStatisticNotFoundException;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.AdvertStatisticService;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.integration.max.dto.MaxSendMessage;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;

import java.util.List;

@State
public class DetailTestInfoStateResolver extends StateResolver {
    private final AdvertStatisticService advertStatisticService;
    private final ClientService clientService;
    private final TelegramIntegrationService telegramIntegrationService;

    @Autowired
    public DetailTestInfoStateResolver(AdvertStatisticService advertStatisticService,
                                       ClientService clientService,
                                       TelegramIntegrationService telegramIntegrationService,
                                       BotViewFacade botViewFacade,
                                       StateStorage<String, List<ClientState>> stateStorage) {
        super(stateStorage, botViewFacade, telegramIntegrationService);
        this.advertStatisticService = advertStatisticService;
        this.clientService = clientService;
        this.telegramIntegrationService = telegramIntegrationService;
    }

    @Override
    public Response resolve(Payload payload) {
        super.resolve(payload);
        String chatId = payload.getChatId();
        String text = payload.getMessage().getText();
        BotType botType = payload.getBotType();

        Response response = new Response(chatId, botType);
        long statisticId;
        try {
            statisticId = Long.parseLong(text);
        } catch (NumberFormatException ex) {

            if (botType == BotType.TELEGRAM) {
                return response.setTelegramResponse(new SendMessage(chatId, "Необходимо ввести целое число!"));
            }

            return response.setMaxSendMessage(new MaxSendMessage().setChatId(chatId).setText("Необходимо ввести целое число!"));
        }

        long clientId = clientService.getByChatId(chatId).getId();
        AdvertStatisticEntity advertStatisticEntity;

        try {
            advertStatisticEntity = advertStatisticService.verifyExistsByClientIdAndId(clientId, statisticId);
        } catch (AdvertStatisticNotFoundException exception) {

            if (botType == BotType.TELEGRAM) {
                return response.setTelegramResponse(new SendMessage(chatId, "В отчете нет строки с ID = " + statisticId));
            }

            return response.setMaxSendMessage(new MaxSendMessage().setChatId(chatId).setText("В отчете нет строки с ID = " + statisticId));
        }

        byte[] photo = advertStatisticEntity.getPhoto();
        telegramIntegrationService.sendImage(chatId, photo);
        String statisticMessage = buildStatisticMessage(advertStatisticEntity);

        if (botType == BotType.TELEGRAM) {
            return response.setTelegramResponse(new SendMessage(chatId, statisticMessage));
        }

        return response.setMaxSendMessage(new MaxSendMessage().setChatId(chatId).setText(statisticMessage));
    }

    @Override
    public ClientState getState() {
        return ClientState.DETAIL_TEST_INFO_MENU;
    }

    private String buildStatisticMessage(AdvertStatisticEntity advertStatistic) {
        StringBuilder sb = new StringBuilder();
        sb.append("1. ID: ").append(advertStatistic.getId()).append("\n");
        sb.append("2. Просмотры: ").append(advertStatistic.getViews()).append("\n");
        sb.append("3. Клики: ").append(advertStatistic.getClicks()).append("\n");
        sb.append("4. CTR: ").append(advertStatistic.getCtr()).append("\n");
        sb.append("5. Затраты: ").append(advertStatistic.getSum()).append("\n");
        sb.append("6. CPC: ").append(advertStatistic.getCpc()).append("\n");
        sb.append("7. Количество добавлений товаров в корзину (atbs): ").append(advertStatistic.getAtbs()).append("\n");
        sb.append("8. Количество заказов: (orders): ").append(advertStatistic.getOrders()).append("\n");
        sb.append("9. Отношение количества заказов к общему количеству посещений кампании (cr): ").append(advertStatistic.getCr()).append("\n");
        sb.append("10. Количество заказанных товаров (shks): ").append(advertStatistic.getShks()).append("\n");
        sb.append("11. Заказов на сумму (sum_price): ").append(advertStatistic.getSumPrice()).append("\n");
        sb.append("12. Идентификатор РК (Advert ID): ").append(advertStatistic.getAdvertEntity().getExternalId()).append("\n");
        sb.append("13. Время запуска теста: ").append(advertStatistic.getAdvertEntity().getStartCheckDateTime().toString());
        return sb.toString();
    }
}
