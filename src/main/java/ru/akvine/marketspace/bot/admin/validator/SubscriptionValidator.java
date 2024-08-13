package ru.akvine.marketspace.bot.admin.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.akvine.marketspace.bot.admin.dto.subscription.SubscriptionRequest;
import ru.akvine.marketspace.bot.constants.ApiErrorConstants;
import ru.akvine.marketspace.bot.exceptions.ValidationException;

@Component
public class SubscriptionValidator extends AdminValidator {
    public void verifySubscriptionRequest(SubscriptionRequest request) {
        verifySecret(request);
        if (StringUtils.isBlank(request.getChatId()) && StringUtils.isBlank(request.getUsername())) {
            throw new ValidationException(
                    ApiErrorConstants.Validation.BOTH_PARAMETERS_BLANK_ERROR,
                    "Username or chat id are not presented. Must  be only one of these params"
            );
        }
        if (StringUtils.isNotBlank(request.getChatId()) && StringUtils.isNotBlank(request.getUsername())) {
            throw new ValidationException(
                    ApiErrorConstants.Validation.BOTH_PARAMETERS_PRESENT_ERROR,
                    "Username and chat id are presented. Must  be only one of these params"
            );
        }
    }
}
