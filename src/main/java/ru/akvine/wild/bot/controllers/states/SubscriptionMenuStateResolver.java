package ru.akvine.wild.bot.controllers.states;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.SubscriptionService;
import ru.akvine.wild.bot.services.domain.SubscriptionModel;
import ru.akvine.wild.bot.services.dto.admin.client.Subscription;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.services.integration.yookassa.YooKassaIntegrationService;
import ru.akvine.wild.bot.utils.DateUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.PAY_SUBSCRIPTION_BUTTON_TEXT;

@State
@Slf4j
public class SubscriptionMenuStateResolver extends StateResolver {
    private final YooKassaIntegrationService yooKassaIntegrationService;
    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionMenuStateResolver(StateStorage<String, List<ClientState>> stateStorage,
                                         BotViewFacade viewFacade,
                                         YooKassaIntegrationService yooKassaIntegrationService,
                                         SubscriptionService subscriptionService,
                                         TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewFacade, telegramIntegrationService);
        this.yooKassaIntegrationService = yooKassaIntegrationService;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public Response resolve(Payload payload) {
        super.resolve(payload);
        String chatId = payload.getChatId();
        String text = payload.getMessage().getText();
        BotType botType = payload.getBotType();

        logger.info("[{}] state resolved", getState());

        if (text.equals(PAY_SUBSCRIPTION_BUTTON_TEXT)) {
            boolean isSuccessfulPayment = yooKassaIntegrationService.tryPayment();
            if (isSuccessfulPayment) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
                SubscriptionModel existedSubscription = subscriptionService.getByChatIdOrNull(chatId);
                if (existedSubscription != null) {
                    String errorMessage = String.format(
                            "Подписка еще активна до %s.\nОплатить можно будет только после этой даты",
                            DateUtils.formatLocalDateTime(existedSubscription.getExpiresAt(), dateTimeFormatter));
                    return new Response(chatId, errorMessage, botType);
                }
                Subscription subscription = new Subscription().setChatId(chatId);
                SubscriptionModel subscriptionModel = subscriptionService.add(subscription);
                String successfulPaymentMessage = String.format(
                        "Платеж прошел успешно! :)\nПодписка оформлена до: %s",
                        DateUtils.formatLocalDateTime(subscriptionModel.getExpiresAt(), dateTimeFormatter));
                return new Response(chatId, successfulPaymentMessage, botType);
            } else {
                return new Response(chatId, "Не удалось провести платеж", botType);
            }
        } else {
            return new Response(chatId, "Нужно выбрать действие из меню", botType);
        }
    }

    @Override
    public ClientState getState() {
        return ClientState.SUBSCRIBE_MENU;
    }
}
