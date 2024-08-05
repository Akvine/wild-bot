package ru.akvine.marketspace.bot.resolvers.state;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.marketspace.bot.entities.AdvertStatisticEntity;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.exceptions.AdvertStatisticNotFoundException;
import ru.akvine.marketspace.bot.infrastructure.state.StateStorage;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.services.AdvertStatisticService;
import ru.akvine.marketspace.bot.services.ClientService;
import ru.akvine.marketspace.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.marketspace.bot.telegram.TelegramData;

@Component
@RequiredArgsConstructor
@Slf4j
public class InputStatisticIdStateResolver implements StateResolver {
    private final TelegramDataResolverManager dataResolverManager;
    private final AdvertStatisticService advertStatisticService;
    private final ClientService clientService;
    private final TelegramIntegrationService telegramIntegrationService;
    private final StateStorage<String> stateStorage;

    @Override
    public BotApiMethod<?> resolve(TelegramData data) {
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(data.getType());
        String chatId = resolver.extractChatId(data.getData());
        String text = resolver.extractText(data.getData());

        logger.info("[{}] state resolved with text = {}", getState(), text);

        long statisticId;
        try {
            statisticId = Long.parseLong(text);
        } catch (NumberFormatException ex) {
            return new SendMessage(chatId, "Необходимо ввести число!");
        }

        long clientId = clientService.getByChatId(chatId).getId();
        AdvertStatisticEntity advertStatisticEntity;

        try {
            advertStatisticEntity = advertStatisticService.verifyExistsByClientIdAndId(clientId, statisticId);
        } catch (AdvertStatisticNotFoundException exception) {
            stateStorage.removeState(chatId);
            return new SendMessage(chatId, "В отчете нет строки с ID = " + statisticId);
        }

        byte[] photo = advertStatisticEntity.getPhoto();
        telegramIntegrationService.sendImage(chatId, photo);
        String statisticMessage = buildStatisticMessage(advertStatisticEntity);
        stateStorage.removeState(chatId);
        return new SendMessage(chatId, statisticMessage);
    }

    @Override
    public ClientState getState() {
        return ClientState.INPUT_STATISTIC_ID;
    }

    @Override
    public void setNextState(String chatId, ClientState nextState) {

    }

    private String buildStatisticMessage(AdvertStatisticEntity advertStatistic) {
        StringBuilder sb = new StringBuilder();
        sb.append("1. ID: ").append(advertStatistic.getId()).append("\n");
        sb.append("2. Просмотры: ").append(advertStatistic.getViews()).append("\n");
        sb.append("3. Клики: ").append(advertStatistic.getClicks()).append("\n");
        sb.append("4. CTR: ").append(advertStatistic.getCtr()).append("\n");
        sb.append("5. CPC: ").append(advertStatistic.getCpc()).append("\n");
        sb.append("6. ATBS: ").append(advertStatistic.getAtbs()).append("\n");
        sb.append("7. ORDERS: ").append(advertStatistic.getOrders()).append("\n");
        sb.append("8. CR: ").append(advertStatistic.getCr()).append("\n");
        sb.append("9. SHKS: ").append(advertStatistic.getShks()).append("\n");
        sb.append("10. SUM_PRICE: ").append(advertStatistic.getSumPrice()).append("\n");
        sb.append("11. Advert ID: ").append(advertStatistic.getAdvertEntity().getAdvertId()).append("\n");
        sb.append("12. Время запуска теста: ").append(advertStatistic.getAdvertEntity().getStartCheckDateTime().toString());
        return sb.toString();
    }
}
