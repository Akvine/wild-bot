package ru.akvine.wild.bot.controllers.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.entities.AdvertStatisticEntity;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.exceptions.AdvertStatisticNotFoundException;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.facades.TelegramDataResolverFacade;
import ru.akvine.wild.bot.facades.TelegramViewFacade;
import ru.akvine.wild.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.wild.bot.services.AdvertStatisticService;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.telegram.TelegramData;

import java.util.List;

@State
public class DetailTestInfoStateResolver extends StateResolver {
    private final AdvertStatisticService advertStatisticService;
    private final ClientService clientService;
    private final TelegramIntegrationService telegramIntegrationService;

    @Autowired
    public DetailTestInfoStateResolver(TelegramDataResolverFacade dataResolverFacade,
                                       AdvertStatisticService advertStatisticService,
                                       ClientService clientService,
                                       TelegramIntegrationService telegramIntegrationService,
                                       TelegramViewFacade telegramViewFacade,
                                       StateStorage<String, List<ClientState>> stateStorage) {
        super(stateStorage, telegramViewFacade, dataResolverFacade, telegramIntegrationService);
        this.advertStatisticService = advertStatisticService;
        this.clientService = clientService;
        this.telegramIntegrationService = telegramIntegrationService;
    }

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        super.resolve(telegramData);
        TelegramDataResolver resolver = dataResolverFacade.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        String text = resolver.extractText(telegramData.getData());

        long statisticId;
        try {
            statisticId = Long.parseLong(text);
        } catch (NumberFormatException ex) {
            return new SendMessage(chatId, "Необходимо ввести целое число!");
        }

        long clientId = clientService.getByChatId(chatId).getId();
        AdvertStatisticEntity advertStatisticEntity;

        try {
            advertStatisticEntity = advertStatisticService.verifyExistsByClientIdAndId(clientId, statisticId);
        } catch (AdvertStatisticNotFoundException exception) {
            return new SendMessage(chatId, "В отчете нет строки с ID = " + statisticId);
        }

        byte[] photo = advertStatisticEntity.getPhoto();
        telegramIntegrationService.sendImage(chatId, photo);
        String statisticMessage = buildStatisticMessage(advertStatisticEntity);
        return new SendMessage(chatId, statisticMessage);
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
