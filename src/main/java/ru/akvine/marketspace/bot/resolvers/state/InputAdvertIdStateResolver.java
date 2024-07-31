package ru.akvine.marketspace.bot.resolvers.state;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.entities.AdvertEntity;
import ru.akvine.marketspace.bot.entities.AdvertStatisticEntity;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.infrastructure.StateStorage;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.services.AdvertService;
import ru.akvine.marketspace.bot.services.AdvertStatisticService;
import ru.akvine.marketspace.bot.services.ClientService;
import ru.akvine.marketspace.bot.services.counter.IterationsCounterService;
import ru.akvine.marketspace.bot.services.domain.AdvertStatisticBean;
import ru.akvine.marketspace.bot.telegram.TelegramData;

@Component
@RequiredArgsConstructor
@Slf4j
public class InputAdvertIdStateResolver implements StateResolver {
    private final StateStorage<String> stateStorage;
    private final TelegramDataResolverManager dataResolverManager;
    private final AdvertService advertService;
    private final AdvertStatisticService advertStatisticService;
    private final ClientService clientService;
    private final IterationsCounterService iterationsCounterService;

    @Override
    public BotApiMethod<?> resolve(TelegramData data) {
        Update update = data.getData();
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(data.getType());
        String chatId = resolver.extractChatId(update);
        String text = resolver.extractText(update);

        logger.info("[{}] state resolved with text = {}", getState(), text);

        int advertId;
        try {
            advertId = Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            return new SendMessage(chatId, "Нужно ввести число!");
        }

        long clientId = clientService.getByChatId(chatId).getId();
        AdvertEntity advertToStop = advertService.verifyExistsByAdvertIdAndClientId(advertId, clientId);
        AdvertStatisticBean advertStatistic = advertStatisticService.getAndSave(advertToStop);
        iterationsCounterService.delete(advertId);
        String message = buildStatisticMessage(advertStatistic);
        stateStorage.removeState(chatId);
        return new SendMessage(chatId, message);
    }

    private String buildStatisticMessage(AdvertStatisticBean advertStatistic) {
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
        sb.append("11. Advert ID: ").append(advertStatistic.getAdvertBean().getAdvertId()).append("\n");
        sb.append("12. Время запуска теста: ").append(advertStatistic.getAdvertBean().getStartCheckDateTime().toString());
        return sb.toString();
    }

    @Override
    public ClientState getState() {
        return ClientState.INPUT_ADVERT_ID;
    }

    @Override
    public void setNextState(String chatId, ClientState nextState) {

    }
}
