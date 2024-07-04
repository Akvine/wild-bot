package ru.akvine.marketspace.bot.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.akvine.marketspace.bot.admin.converters.AdvertConverter;
import ru.akvine.marketspace.bot.admin.dto.advert.ListAdvertRequest;
import ru.akvine.marketspace.bot.admin.dto.advert.PauseAdvertRequest;
import ru.akvine.marketspace.bot.admin.dto.advert.RenameAdvertRequest;
import ru.akvine.marketspace.bot.admin.dto.common.Response;
import ru.akvine.marketspace.bot.admin.dto.common.SuccessfulResponse;
import ru.akvine.marketspace.bot.admin.meta.AdvertControllerMeta;
import ru.akvine.marketspace.bot.admin.validator.AdvertValidator;
import ru.akvine.marketspace.bot.services.admin.AdvertAdminService;
import ru.akvine.marketspace.bot.services.domain.AdvertBean;
import ru.akvine.marketspace.bot.services.dto.admin.advert.ListAdvert;
import ru.akvine.marketspace.bot.services.dto.admin.advert.PauseAdvert;
import ru.akvine.marketspace.bot.services.dto.admin.advert.RenameAdvert;

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
        advertAdminService.pauseAdvert(pauseAdvert);
        return new SuccessfulResponse();
    }

    @Override
    public Response list(@Valid ListAdvertRequest request) {
        advertValidator.verifyListAdvertRequest(request);
        ListAdvert listAdvert = advertConverter.convertToListAdvert(request);
        List<AdvertBean> adverts = advertAdminService.listAdvert(listAdvert);
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
