package ru.akvine.wild.bot.admin.validator;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.admin.dto.advert.ListAdvertRequest;
import ru.akvine.wild.bot.admin.dto.advert.PauseAdvertRequest;
import ru.akvine.wild.bot.admin.dto.advert.RenameAdvertRequest;
import ru.akvine.wild.bot.exceptions.ValidationException;
import ru.akvine.wild.bot.constants.ApiErrorConstants;
import ru.akvine.wild.bot.validator.AdvertStatusValidator;

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

    public void verifyUuidAndIdParams(String uuid, Integer id) {
        if (StringUtils.isNotBlank(uuid) && id != null) {
            throw new ValidationException(
                    ApiErrorConstants.Validation.BOTH_PARAMETERS_PRESENT_ERROR,
                    "Presented advertId and advertUuid. Need only one parameter"
            );
        }

        if (StringUtils.isBlank(uuid) && id == null) {
            throw new ValidationException(
                    ApiErrorConstants.Validation.BOTH_PARAMETERS_BLANK_ERROR,
                    "Blank advertId and advertUuid. Need only one parameter"
            );
        }
    }
}
