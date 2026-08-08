package ru.akvine.wild.bot.controllers.states;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.TelegramViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;

import java.util.List;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.QUERY_QR_CODE_BUTTON_TEXT;

@State
public class FillAdvertisingStateResolver extends StateResolver {

    @Autowired
    public FillAdvertisingStateResolver(StateStorage<String, List<ClientState>> stateStorage,
                                        TelegramViewFacade telegramViewFacade,
                                        TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, telegramViewFacade, telegramIntegrationService);
    }

    @Override
    public Response resolve(Payload payload) {
        super.resolve(payload);
        String chatId = payload.getChatId();
        String text = payload.getMessage().getText();
        BotType botType = payload.getBotType();

        Response response = new Response(chatId, botType);
        if (text.equals(QUERY_QR_CODE_BUTTON_TEXT)) {
            if (botType == BotType.TELEGRAM) {
                return response.setTelegramResponse(new SendMessage(chatId, "Спасибо! В ближайшее время бот отправит вам QR-код на пополнение бюджета :-)"));
            }

            return response.setText("Спасибо! В ближайшее время бот отправит вам QR-код на пополнение бюджета :-)");
        } else {
            if (botType == BotType.TELEGRAM) {
                return response.setTelegramResponse(new SendMessage(chatId, "Вывберите действие из меню"));
            }

            return response.setText("Вывберите действие из меню");
        }
    }

    @Override
    public ClientState getState() {
        return ClientState.FILL_ADVERTISING_ACCOUNT_MENU;
    }
}
