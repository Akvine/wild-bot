package ru.akvine.marketspace.bot.admin.validator;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.akvine.marketspace.bot.admin.dto.advert.ListAdvertRequest;
import ru.akvine.marketspace.bot.admin.dto.advert.PauseAdvertRequest;
import ru.akvine.marketspace.bot.admin.dto.advert.RenameAdvertRequest;
import ru.akvine.marketspace.bot.exceptions.ValidationException;
import ru.akvine.marketspace.bot.exceptions.handler.CommonErrorCodes;
import ru.akvine.marketspace.bot.validator.AdvertStatusValidator;

@Component
@RequiredArgsConstructor
public class AdvertValidator extends AdminValidator {
    private final AdvertStatusValidator advertStatusValidator;

    public void verifyPauseAdvertRequest(PauseAdvertRequest request) {
        verifySecret(request);
        verifyUuidAndIdParams(request.getAdvertUuid(), request.getAdvertId());
    }

    public void verifyListAdvertRequest(ListAdvertRequest request) {
        verifySecret(request);
        request.getStatuses().forEach(advertStatusValidator::validate);
    }

    public void verifyRenameAdvertRequest(RenameAdvertRequest request) {
        verifySecret(request);
        verifyUuidAndIdParams(request.getAdvertUuid(), request.getAdvertId());
    }

    public void verifyUuidAndIdParams(String uuid, String id) {
        if (StringUtils.isNotBlank(uuid) && StringUtils.isNotBlank(id)) {
            throw new ValidationException(
                    CommonErrorCodes.Validation.BOTH_PARAMETERS_PRESENT_ERROR,
                    "Presented advertId and advertUuid. Need only one parameter"
            );
        }

        if (StringUtils.isBlank(uuid) && StringUtils.isBlank(id)) {
            throw new ValidationException(
                    CommonErrorCodes.Validation.BOTH_PARAMETERS_BLANK_ERROR,
                    "Blank advertId and advertUuid. Need only one parameter"
            );
        }
    }
}
