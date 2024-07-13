package ru.akvine.marketspace.bot.admin.validator;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.akvine.marketspace.bot.admin.dto.client.*;
import ru.akvine.marketspace.bot.exceptions.ValidationException;
import ru.akvine.marketspace.bot.exceptions.handler.CommonErrorCodes;

@Component
public class ClientValidator extends AdminValidator {
    @Value("${max.clients.send.message.count}")
    private int maxClientsSendMessageCount;

    public void verifySendMessageRequest(SendMessageRequest request) {
        verifySecret(request);

        if (!CollectionUtils.isEmpty(request.getChatIds()) && request.getChatIds().size() > maxClientsSendMessageCount) {
            String errorMessage = String.format(
                    "Clients count to send message = [%s] is greater than max = [%s]",
                    request.getChatIds().size(), maxClientsSendMessageCount);
            throw new ValidationException(CommonErrorCodes.Validation.MAX_CLIENTS_SEND_MESSAGE_COUNT_ERROR, errorMessage);
        }

        if (!CollectionUtils.isEmpty(request.getUsernames()) && request.getUsernames().size() > maxClientsSendMessageCount) {
            String errorMessage = String.format(
                    "Clients count to send message = [%s] is greater than max = [%s]",
                    request.getChatIds().size(), maxClientsSendMessageCount);
            throw new ValidationException(CommonErrorCodes.Validation.MAX_CLIENTS_SEND_MESSAGE_COUNT_ERROR, errorMessage);
        }
    }

    public void verifyBlockClientRequest(BlockClientRequest request) {
        verifySecret(request);
        Preconditions.checkNotNull(request, "blockClientRequest is null");
        verifyBlockRequest(request);
    }

    public void verifyUnblockClientRequest(UnblockClientRequest request) {
        verifySecret(request);
        Preconditions.checkNotNull(request, "unblockClientRequest is null");
        verifyBlockRequest(request);
    }

    public void verifyWhitelistRequest(WhitelistRequest request) {
        verifySecret(request);
        if (StringUtils.isBlank(request.getChatId()) && StringUtils.isBlank(request.getUsername())) {
            throw new ValidationException(
                    CommonErrorCodes.Validation.BOTH_PARAMETERS_BLANK_ERROR,
                    "Username or chat id are not presented. Must  be only one of these params"
            );
        }
        if (StringUtils.isNotBlank(request.getChatId()) && StringUtils.isNotBlank(request.getUsername())) {
            throw new ValidationException(
                    CommonErrorCodes.Validation.BOTH_PARAMETERS_PRESENT_ERROR,
                    "Username and chat id are presented. Must  be only one of these params"
            );
        }
    }

    private void verifyBlockRequest(BlockRequest request) {
        if (StringUtils.isBlank(request.getUuid()) && StringUtils.isBlank(request.getChatId()) && StringUtils.isBlank(request.getUsername())) {
            throw new ValidationException(
                    CommonErrorCodes.Validation.BOTH_PARAMETERS_BLANK_ERROR,
                    "Username, chatId and client uuid are not presented. Must be only one of these params");
        }
        if (StringUtils.isNotBlank(request.getUuid()) && StringUtils.isNotBlank(request.getChatId()) && StringUtils.isNotBlank(request.getUsername())) {
            throw new ValidationException(
                    CommonErrorCodes.Validation.BOTH_PARAMETERS_PRESENT_ERROR,
                    "Username, chatId and client uuid are presented. Must be only one of these params");
        }
    }
}
