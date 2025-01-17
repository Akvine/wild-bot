package ru.akvine.wild.bot.controllers.states;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.TelegramDataResolverFacade;
import ru.akvine.wild.bot.facades.TelegramViewFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.resolvers.data.TelegramDataResolver;
import ru.akvine.wild.bot.services.SubscriptionService;
import ru.akvine.wild.bot.services.domain.SubscriptionModel;
import ru.akvine.wild.bot.services.dto.admin.client.Subscription;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.services.integration.yookassa.YooKassaIntegrationService;
import ru.akvine.wild.bot.telegram.TelegramData;
import ru.akvine.wild.bot.utils.DateUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.akvine.wild.bot.constants.telegram.TelegramButtonConstants.PAY_SUBSCRIPTION_BUTTON_TEXT;

@State
@Slf4j
public class SubscriptionMenuStateResolver extends StateResolver {
    private final TelegramDataResolverFacade dataResolverFacade;
    private final YooKassaIntegrationService yooKassaIntegrationService;
    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionMenuStateResolver(StateStorage<String, List<ClientState>> stateStorage,
                                         TelegramViewFacade viewFacade,
                                         TelegramDataResolverFacade dataResolverFacade,
                                         YooKassaIntegrationService yooKassaIntegrationService,
                                         SubscriptionService subscriptionService,
                                         TelegramIntegrationService telegramIntegrationService) {
        super(stateStorage, viewFacade, dataResolverFacade, telegramIntegrationService);
        this.dataResolverFacade = dataResolverFacade;
        this.yooKassaIntegrationService = yooKassaIntegrationService;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public BotApiMethod<?> resolve(TelegramData telegramData) {
        TelegramDataResolver resolver = dataResolverFacade.getTelegramDataResolvers().get(telegramData.getType());
        String chatId = resolver.extractChatId(telegramData.getData());
        String text = resolver.extractText(telegramData.getData());

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
                    return new SendMessage(chatId, errorMessage);
                }
                Subscription subscription = new Subscription().setChatId(chatId);
                SubscriptionModel subscriptionModel = subscriptionService.add(subscription);
                String successfulPaymentMessage = String.format(
                        "Платеж прошел успешно! :)\nПодписка оформлена до: %s",
                        DateUtils.formatLocalDateTime(subscriptionModel.getExpiresAt(), dateTimeFormatter));
                return new SendMessage(chatId, successfulPaymentMessage);
            } else {
                return new SendMessage(chatId, "Не удалось провести платеж");
            }
        } else {
            return new SendMessage(chatId, "Нужно выбрать действие из меню");
        }
    }

    @Override
    public ClientState getState() {
        return ClientState.SUBSCRIBE_MENU;
    }
}
