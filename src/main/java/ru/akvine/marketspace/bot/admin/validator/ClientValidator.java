package ru.akvine.marketspace.bot.admin.validator;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.akvine.marketspace.bot.admin.dto.client.BlockClientRequest;
import ru.akvine.marketspace.bot.admin.dto.client.BlockRequest;
import ru.akvine.marketspace.bot.admin.dto.client.UnblockClientRequest;
import ru.akvine.marketspace.bot.exceptions.ValidationException;
import ru.akvine.marketspace.bot.exceptions.handler.CommonErrorCodes;

@Component
public class ClientValidator extends AdminValidator {
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
