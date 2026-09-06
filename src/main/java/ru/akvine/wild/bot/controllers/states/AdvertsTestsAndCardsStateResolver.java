package ru.akvine.wild.bot.controllers.states;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.controllers.converters.StartedAdvertsConverter;
import ru.akvine.wild.bot.enums.AdvertStatus;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.AdvertService;
import ru.akvine.wild.bot.services.CardService;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.domain.AdvertModel;
import ru.akvine.wild.bot.services.domain.CardModel;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.utils.DateUtils;

@State
public class AdvertsTestsAndCardsStateResolver extends StateResolver {
    private final ClientService clientService;
    private final CardService cardService;
    private final AdvertService advertService;
    private final StartedAdvertsConverter startedAdvertsConverter;

    private static final String NEW_LINE = "\n";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");

    @Autowired
    public AdvertsTestsAndCardsStateResolver(
            StateStorage<String, List<ClientState>> stateStorage,
            BotViewFacade viewFacade,
            TelegramIntegrationService telegramIntegrationService,
            ClientService clientService,
            CardService cardService,
            AdvertService advertService,
            StartedAdvertsConverter startedAdvertsConverter) {
        super(stateStorage, viewFacade, telegramIntegrationService);
        this.clientService = clientService;
        this.cardService = cardService;
        this.advertService = advertService;
        this.startedAdvertsConverter = startedAdvertsConverter;
    }

    @Override
    @Nullable
    public Response resolve(Payload payload) {
        super.resolve(payload);
        String chatId = payload.getChatId();
        String text = payload.getMessage().getText();
        BotType botType = payload.getBotType();

        Response response = new Response(chatId, botType);
        if (text.equals(LIST_STARTED_TESTS_BUTTON_TEXT)) {
            List<AdvertModel> runningAdverts = advertService.getAdvertsByChatIdAndBotTypeAndStatuses(
                    chatId, botType, List.of(AdvertStatus.RUNNING));
            int availableTestsCount =
                    clientService.getByChatIdAndBotType(chatId, botType).getAvailableTestsCount();
            return response.setText(
                    startedAdvertsConverter.convertToStartedListMessage(chatId, availableTestsCount, runningAdverts));
        } else if (text.equals(LIST_ADVERTS_BUTTON_TEXT)) {
            return response.setText(buildAdvertsListMessage(chatId, botType));
        } else if (text.equals(LIST_CARDS_BUTTON_TEXT)) {
            return response.setText(buildCardsListMessage(chatId, botType));
        } else {
            return resolveDefaultResponse(chatId, botType);
        }
    }

    private String buildAdvertsListMessage(String chatId, BotType botType) {
        List<AdvertModel> adverts = advertService.getByChatIdAndBotType(chatId, botType);
        StringBuilder sb = new StringBuilder();

        int lastElementIndex = adverts.size() - 1;
        sb.append("===============================").append(NEW_LINE);
        for (int i = 0; i < adverts.size(); ++i) {
            sb.append("ID: ")
                    .append(adverts.get(i).getId())
                    .append(NEW_LINE)
                    .append("Назание: ")
                    .append(adverts.get(i).getName())
                    .append(NEW_LINE)
                    .append("Статус: ")
                    .append(adverts.get(i).getStatus())
                    .append(NEW_LINE)
                    .append("Дата создания: ")
                    .append(DateUtils.formatLocalDateTime(adverts.get(i).getCreatedDate(), DATE_TIME_FORMATTER))
                    .append(NEW_LINE);

            sb.append("===============================");
            if (lastElementIndex != i) {
                sb.append(NEW_LINE);
            }
        }

        return sb.toString();
    }

    private String buildCardsListMessage(String chatId, BotType botType) {
        List<CardModel> cards = cardService.getByChatIdAndBotType(chatId, botType);
        StringBuilder sb = new StringBuilder();

        int lastElementIndex = cards.size() - 1;
        sb.append("===============================").append(NEW_LINE);
        for (int i = 0; i < cards.size(); ++i) {
            sb.append("ID: ")
                    .append(cards.get(i).getId())
                    .append(NEW_LINE)
                    .append("Название: ")
                    .append(cards.get(i).getExternalTitle())
                    .append(NEW_LINE)
                    .append("Тип: ")
                    .append(cards.get(i).getCardType().getType())
                    .append(NEW_LINE)
                    .append("Дата создания: ")
                    .append(DateUtils.formatLocalDateTime(cards.get(i).getCreatedDate(), DATE_TIME_FORMATTER))
                    .append(NEW_LINE);

            sb.append("===============================");
            if (lastElementIndex != i) {
                sb.append(NEW_LINE);
            }
        }

        return sb.toString();
    }

    @Override
    public ClientState getState() {
        return ClientState.ADVERTS_TESTS_CARDS_MENU;
    }
}
