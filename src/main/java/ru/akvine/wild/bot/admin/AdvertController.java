package ru.akvine.wild.bot.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.akvine.wild.bot.admin.converters.AdvertConverter;
import ru.akvine.wild.bot.admin.dto.advert.ListAdvertRequest;
import ru.akvine.wild.bot.admin.dto.advert.PauseAdvertRequest;
import ru.akvine.wild.bot.admin.dto.advert.RenameAdvertRequest;
import ru.akvine.wild.bot.admin.dto.common.Response;
import ru.akvine.wild.bot.admin.dto.common.SuccessfulResponse;
import ru.akvine.wild.bot.admin.meta.AdvertControllerMeta;
import ru.akvine.wild.bot.admin.validator.AdvertValidator;
import ru.akvine.wild.bot.services.admin.AdvertAdminService;
import ru.akvine.wild.bot.services.domain.AdvertModel;
import ru.akvine.wild.bot.services.domain.AdvertStatisticModel;
import ru.akvine.wild.bot.services.dto.admin.advert.ListAdvert;
import ru.akvine.wild.bot.services.dto.admin.advert.PauseAdvert;
import ru.akvine.wild.bot.services.dto.admin.advert.RenameAdvert;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdvertController implements AdvertControllerMeta {
    private final AdvertValidator advertValidator;
    private final AdvertConverter advertConverter;
    private final AdvertAdminService advertAdminService;

    @Override
    public Response pause(@Valid PauseAdvertRequest request) {
        advertValidator.verifyPauseAdvertRequest(request);
        PauseAdvert pauseAdvert = advertConverter.convertToPauseAdvert(request);
        AdvertStatisticModel advertStatisticBean = advertAdminService.pauseAdvert(pauseAdvert);
        return advertConverter.convertToPauseAdvert(advertStatisticBean);
    }

    @Override
    public Response pauseForce(@Valid PauseAdvertRequest request) {
        advertValidator.verifyPauseAdvertRequest(request);
        PauseAdvert pauseAdvert = advertConverter.convertToPauseAdvert(request);
        advertAdminService.pauseAdvertForce(pauseAdvert);
        return new SuccessfulResponse();
    }

    @Override
    public Response list(@Valid ListAdvertRequest request) {
        advertValidator.verifyListAdvertRequest(request);
        ListAdvert listAdvert = advertConverter.convertToListAdvert(request);
        List<AdvertModel> adverts = advertAdminService.listAdvert(listAdvert);
        return advertConverter.convertToAdvertListResponse(adverts);
    }

    @Override
    public Response rename(@Valid RenameAdvertRequest request) {
        advertValidator.verifyRenameAdvertRequest(request);
        RenameAdvert renameAdvert = advertConverter.convertToRenameAdvert(request);
        advertAdminService.renameAdvert(renameAdvert);
        return new SuccessfulResponse();
    }
}
