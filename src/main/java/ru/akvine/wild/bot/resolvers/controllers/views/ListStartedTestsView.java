package ru.akvine.wild.bot.resolvers.controllers.views;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.akvine.wild.bot.resolvers.controllers.converters.StartedAdvertsConverter;
import ru.akvine.wild.bot.enums.AdvertStatus;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.services.AdvertService;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.domain.AdvertModel;
import ru.akvine.wild.bot.telegram.KeyboardFactory;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListStartedTestsView implements TelegramView {
    private final StartedAdvertsConverter startedAdvertsConverter;
    private final AdvertService advertService;
    private final ClientService clientService;

    @Override
    public InlineKeyboardMarkup getKeyboard(String chatId) {
        return KeyboardFactory.getBackKeyboard();
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
