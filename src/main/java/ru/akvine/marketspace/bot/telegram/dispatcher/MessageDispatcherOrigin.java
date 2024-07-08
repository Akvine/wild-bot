package ru.akvine.marketspace.bot.telegram.dispatcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.akvine.marketspace.bot.constants.ClientStates;
import ru.akvine.marketspace.bot.context.ClientData;
import ru.akvine.marketspace.bot.context.ClientDataContext;
import ru.akvine.marketspace.bot.controller.AdvertStartController;
import ru.akvine.marketspace.bot.controller.StatisticController;
import ru.akvine.marketspace.bot.exceptions.BlockedCredentialsException;
import ru.akvine.marketspace.bot.services.BlockingService;
import ru.akvine.marketspace.bot.services.ClientService;
import ru.akvine.marketspace.bot.services.ReportService;
import ru.akvine.marketspace.bot.services.domain.ClientBean;
import ru.akvine.marketspace.bot.services.dto.ClientCreate;
import ru.akvine.marketspace.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.marketspace.bot.telegram.CommandResolver;
import ru.akvine.marketspace.bot.telegram.TelegramClientStateResolver;
import ru.akvine.marketspace.bot.utils.TelegramPhotoResolver;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageDispatcherOrigin implements MessageDispatcher {
    private final ClientService clientService;
    private final BlockingService blockingService;
    private final TelegramIntegrationService telegramIntegrationService;
    private final ReportService reportService;

    private final AdvertStartController advertStartController;
    private final StatisticController statisticController;

    private final CommandResolver commandResolver;
    private final TelegramClientStateResolver telegramClientStateResolver;
    private final TelegramPhotoResolver telegramPhotoResolver;

    private final static String REPORT_DEFAULT_FILE_NAME = "report.xlsx";
    private final static String DEFAULT_OUTPUT_MESSAGE = "Введите команду: ";

    @Value("${test.start.advert.mode.enabled}")
    private boolean isTestStartAdvertModeEnabled;

    public BotApiMethod<?> doDispatch(Update update) {
        Message message = update.getMessage();
        String chatId;
        String text;
        if (update.getCallbackQuery() != null) {
            chatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
            text = update.getCallbackQuery().getData();
        } else {
            chatId = String.valueOf(message.getChatId());
            text = message.getText();
        }

        ClientBean clientBean = clientService.findByChatId(chatId);
        if (clientBean == null || clientBean.isDeleted()) {
            ClientCreate clientCreate = new ClientCreate(
                    chatId,
                    message.getFrom().getUserName(),
                    message.getFrom().getFirstName(),
                    message.getFrom().getLastName()
            );
            clientBean = clientService.create(clientCreate);
        } else {
            checkIsBlockedAndThrowException(clientBean.getUuid());
        }

        logger.info("Send message = {} by client = [{}]", text, clientBean);
        String clientUuid = clientBean.getUuid();

        if (telegramClientStateResolver.containsState(clientUuid)) {
            if (commandResolver.isCancelCommand(text)) {
                telegramClientStateResolver.removeState(clientUuid);
                return new SendMessage(chatId, DEFAULT_OUTPUT_MESSAGE);
            }
            return processStates(update, chatId, clientUuid);
        }

        if (commandResolver.isStartCommand(text)) {
            SendMessage sendMessage = advertStartController.getCategories(chatId);
            telegramClientStateResolver.setState(clientUuid, ClientStates.START_ADVERT.CHOOSE_CATEGORY_STATE);
            return sendMessage;
        } else if (commandResolver.isStatisticCommand(text)) {
            return statisticController.getStatistic(chatId);
        } else if (commandResolver.isHelpCommand(text)) {
            return new SendMessage(chatId, buildHelpMessage());
        } else if (commandResolver.isReportCommand(text)) {
            byte[] report = reportService.generateReport(chatId);
            telegramIntegrationService.sendFile(new ByteArrayInputStream(report), REPORT_DEFAULT_FILE_NAME, chatId);
            return new SendMessage(chatId, DEFAULT_OUTPUT_MESSAGE);
        }
        return new SendMessage(
                chatId,
                "Неизвестная команда. Введите /help для просмотра списка доступных команд"
        );
    }

    private BotApiMethod<?> processStates(Update update, String chatId, String clientUuid) {
        if (ClientStates.START_ADVERT.CHOOSE_CATEGORY_STATE.equals(telegramClientStateResolver.getState(clientUuid))) {
            String categoryId = update.getCallbackQuery().getData();
            ClientDataContext.set(new ClientData().setCategoryId(categoryId));
            telegramClientStateResolver.setState(clientUuid, ClientStates.START_ADVERT.UPLOAD_PHOTO_STATE);
            return new SendMessage(chatId, "Загрузите новое изображение карточки: ");
        } else if (ClientStates.START_ADVERT.UPLOAD_PHOTO_STATE.equals(telegramClientStateResolver.getState(clientUuid))) {
            telegramClientStateResolver.removeState(clientUuid);
            PhotoSize photoSize = telegramPhotoResolver.resolve(update.getMessage());
            byte[] photo = telegramIntegrationService.downloadPhoto(photoSize.getFileId(), chatId);
            Objects.requireNonNull(ClientDataContext.get()).setNewCardPhoto(photo);

            if (isTestStartAdvertModeEnabled) {
                // TODO : чисто для тестов это условие. После успешного запуска MVP можно убрать
                return advertStartController.startAdvertById(chatId);
            }
            return advertStartController.startAdvert(chatId);
        } else {
            throw new IllegalStateException("Illegal state");
        }
    }

    private void checkIsBlockedAndThrowException(String clientUuid) {
        LocalDateTime blockDateTime = blockingService.getEndBlockDate(clientUuid);
        if (blockDateTime != null) {
            String errorMessage = String.format("Вы были заблокированы до %s!", blockDateTime.toLocalDate());
            throw new BlockedCredentialsException(errorMessage);
        }
    }

    private String buildHelpMessage() {
        StringBuilder sb = new StringBuilder();
        return sb
                .append("/start - запустить кампанию в паузе или готовую к запуску \n")
                .append("/statistic - вывести статистику по запущенным кампаниям \n")
                .append("/report - генерирует отчет с тестами \n")
                .append("/help - список доступных команд \n")
                .append("/cancel - отменить запуск кампании")
                .toString();
    }
}
