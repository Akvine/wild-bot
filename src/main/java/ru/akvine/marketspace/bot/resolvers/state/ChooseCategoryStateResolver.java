package ru.akvine.marketspace.bot.resolvers.state;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.enums.ClientState;
import ru.akvine.marketspace.bot.infrastructure.SessionStorage;
import ru.akvine.marketspace.bot.infrastructure.StateStorage;
import ru.akvine.marketspace.bot.infrastructure.impl.ClientSessionData;
import ru.akvine.marketspace.bot.managers.TelegramDataResolverManager;
import ru.akvine.marketspace.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.marketspace.bot.telegram.TelegramData;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChooseCategoryStateResolver implements StateResolver {
    private final SessionStorage<String, ClientSessionData> sessionStorage;
    private final StateStorage<String> stateStorage;
    private final TelegramDataResolverManager dataResolverManager;

    @Override
    public BotApiMethod<?> resolve(TelegramData data) {
        Update update = data.getData();
        TelegramDataResolver resolver = dataResolverManager.getTelegramDataResolvers().get(data.getType());
        String chatId = resolver.extractChatId(update);
        Integer categoryId;

        try {
            categoryId = Integer.parseInt(resolver.extractText(data.getData()));
        } catch (Exception exception) {
            return new SendMessage(chatId, "Нужно выбрать категорию из списка или ввести id вручную!");
        }

        logger.info("[{}] state resolved for chat with id = {} and category id = {}", getState(), chatId, categoryId);

        sessionStorage.get(chatId).setChoosenCategoryId(categoryId);
        setNextState(chatId, ClientState.UPLOAD_NEW_CARD_PHOTO_STATE);
        return new SendMessage(chatId, "Загрузите новое изображение карточки: ");
    }

    @Override
    public ClientState getState() {
        return ClientState.CHOOSE_CATEGORY_STATE;
    }

    @Override
    public void setNextState(String chatId, ClientState nextState) {
        stateStorage.setState(chatId, nextState);
    }
}
