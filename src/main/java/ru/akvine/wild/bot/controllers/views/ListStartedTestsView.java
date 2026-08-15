package ru.akvine.wild.bot.controllers.views;

import java.util.List;
import ru.akvine.wild.bot.controllers.converters.StartedAdvertsConverter;
import ru.akvine.wild.bot.enums.AdvertStatus;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;
import ru.akvine.wild.bot.services.AdvertService;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.domain.AdvertModel;

@View
public class ListStartedTestsView extends AbstractBotView {
    private final StartedAdvertsConverter startedAdvertsConverter;
    private final AdvertService advertService;
    private final ClientService clientService;

    public ListStartedTestsView(
            BotKeyboardFactoryFacade facade,
            StartedAdvertsConverter startedAdvertsConverter,
            AdvertService advertService,
            ClientService clientService) {
        super(facade);
        this.startedAdvertsConverter = startedAdvertsConverter;
        this.advertService = advertService;
        this.clientService = clientService;
    }

    @Override
    public String getMessage(String chatId, BotType botType) {
        List<AdvertModel> runningAdverts =
                advertService.getAdvertsByChatIdAndBotTypeAndStatuses(chatId, botType, List.of(AdvertStatus.RUNNING));
        int availableTestsCount =
                clientService.getByChatIdAndBotType(chatId, botType).getAvailableTestsCount();
        return startedAdvertsConverter.convertToStartedListMessage(chatId, availableTestsCount, runningAdverts);
    }

    @Override
    public ClientState byState() {
        return ClientState.LIST_STARTED_TESTS_MENU;
    }
}
