package ru.akvine.wild.bot.admin.validator;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.akvine.wild.bot.admin.dto.client.*;
import ru.akvine.wild.bot.constants.ApiErrorConstants;
import ru.akvine.wild.bot.exceptions.ValidationException;
import ru.akvine.wild.bot.validator.BotTypeValidator;

@Component
@RequiredArgsConstructor
public class ClientValidator {
    @Value("${max.clients.send.message.count}")
    private int maxClientsSendMessageCount;

    private final BotTypeValidator botTypeValidator;

    public void verifyAddTestsRequest(AddTestsRequest request) {

        if (StringUtils.isBlank(request.getUsername()) && StringUtils.isBlank(request.getChatId())) {
            throw new ValidationException(
                    ApiErrorConstants.Validation.BOTH_PARAMETERS_BLANK_ERROR,
                    "Username and chatId parameters is blank");
        }

        if (request.getCount() < 1) {
            throw new ValidationException(
                    ApiErrorConstants.Validation.LESS_THEN_MIN_VALUE_ERROR, "Count parameter can't be less than 1");
        }

        botTypeValidator.validate(request.getBotType());
    }

    public void verifySendMessageRequest(SendMessageRequest request) {

        if (!CollectionUtils.isEmpty(request.getChatIds())
                && request.getChatIds().size() > maxClientsSendMessageCount) {
            String errorMessage = String.format(
                    "Clients count to send message = [%s] is greater than max = [%s]",
                    request.getChatIds().size(), maxClientsSendMessageCount);
            throw new ValidationException(
                    ApiErrorConstants.Validation.MAX_CLIENTS_SEND_MESSAGE_COUNT_ERROR, errorMessage);
        }

        if (!CollectionUtils.isEmpty(request.getUsernames())
                && request.getUsernames().size() > maxClientsSendMessageCount) {
            String errorMessage = String.format(
                    "Clients count to send message = [%s] is greater than max = [%s]",
                    request.getChatIds().size(), maxClientsSendMessageCount);
            throw new ValidationException(
                    ApiErrorConstants.Validation.MAX_CLIENTS_SEND_MESSAGE_COUNT_ERROR, errorMessage);
        }

        botTypeValidator.validate(request.getBotType());
    }

    public void verifyBlockClientRequest(BlockClientRequest request) {
        Preconditions.checkNotNull(request, "blockClientRequest is null");
        verifyBlockRequest(request);
    }

    public void verifyUnblockClientRequest(UnblockClientRequest request) {
        Preconditions.checkNotNull(request, "unblockClientRequest is null");
        verifyBlockRequest(request);
    }

    public void verifyBotType(String botType) {
        botTypeValidator.validate(botType);
    }

    private void verifyBlockRequest(BlockRequest request) {
        if (StringUtils.isBlank(request.getUuid())
                && StringUtils.isBlank(request.getChatId())
                && StringUtils.isBlank(request.getUsername())) {
            throw new ValidationException(
                    ApiErrorConstants.Validation.BOTH_PARAMETERS_BLANK_ERROR,
                    "Username, chatId and client uuid are not presented. Must be only one of these params");
        }
        if (StringUtils.isNotBlank(request.getUuid())
                && StringUtils.isNotBlank(request.getChatId())
                && StringUtils.isNotBlank(request.getUsername())) {
            throw new ValidationException(
                    ApiErrorConstants.Validation.BOTH_PARAMETERS_PRESENT_ERROR,
                    "Username, chatId and client uuid are presented. Must be only one of these params");
        }

        if (StringUtils.isNotBlank(request.getChatId())
                && (StringUtils.isBlank(request.getUsername()) || StringUtils.isBlank(request.getUuid()))) {
            botTypeValidator.validate(request.getBotType());
        }

        if (StringUtils.isNotBlank(request.getBotType())
                && (StringUtils.isBlank(request.getUsername()) || StringUtils.isBlank(request.getUuid()))) {
            if (StringUtils.isBlank(request.getChatId())) {
                throw new ValidationException(
                        ApiErrorConstants.Validation.CHAT_ID_IS_NOT_PRESENTED_ERROR,
                        "ChatId field is blank. Must be presented if has botType");
            }
        }
    }
}
