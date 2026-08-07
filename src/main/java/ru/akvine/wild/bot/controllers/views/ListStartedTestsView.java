package ru.akvine.wild.bot.controllers.views;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.wild.bot.bot.dto.InlineKeyboard;
import ru.akvine.wild.bot.controllers.converters.StartedAdvertsConverter;
import ru.akvine.wild.bot.enums.AdvertStatus;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotKeyboardFactoryFacade;
import ru.akvine.wild.bot.infrastructure.annotations.View;
import ru.akvine.wild.bot.services.AdvertService;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.domain.AdvertModel;
import ru.akvine.wild.bot.telegram.TelegramKeyboardFactory;

import java.util.List;

@View
@RequiredArgsConstructor
public class ListStartedTestsView implements BotView {
    private final StartedAdvertsConverter startedAdvertsConverter;
    private final AdvertService advertService;
    private final ClientService clientService;

    private final BotKeyboardFactoryFacade facade;

    @Override
    public InlineKeyboard getKeyboard(String chatId, BotType botType) {
        return facade.resolve(botType, byState()).create(chatId);
    }

    @Override
    public String getMessage(String chatId) {
        List<AdvertModel> runningAdverts = advertService.getAdvertsByChatIdAndStatuses(chatId, List.of(AdvertStatus.RUNNING));
        int availableTestsCount = clientService.getByChatId(chatId).getAvailableTestsCount();
        return startedAdvertsConverter.convertToStartedListMessage(chatId, availableTestsCount, runningAdverts);
    }

    @Override
    public ClientState byState() {
        return ClientState.LIST_STARTED_TESTS_MENU;
    }
}
