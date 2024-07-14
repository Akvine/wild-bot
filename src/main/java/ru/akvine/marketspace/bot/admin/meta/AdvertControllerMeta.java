package ru.akvine.marketspace.bot.admin.meta;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.akvine.marketspace.bot.admin.dto.advert.ListAdvertRequest;
import ru.akvine.marketspace.bot.admin.dto.advert.PauseAdvertRequest;
import ru.akvine.marketspace.bot.admin.dto.advert.RenameAdvertRequest;
import ru.akvine.marketspace.bot.admin.dto.advert.StartAdvertRequest;
import ru.akvine.marketspace.bot.admin.dto.common.Response;

@RequestMapping(value = "/admin/adverts")
public interface AdvertControllerMeta {

    @PostMapping(value = "/start")
    Response start(@Valid @RequestBody StartAdvertRequest request);

    @PostMapping(value = "/pause")
    Response pause(@Valid @RequestBody PauseAdvertRequest request);

    @PostMapping(value = "/pause/force")
    Response pauseForce(@Valid @RequestBody PauseAdvertRequest request);

    @PostMapping(value = "/list")
    Response list(@Valid @RequestBody ListAdvertRequest request);

    @PostMapping(value = "/rename")
    Response rename(@Valid @RequestBody RenameAdvertRequest request);
}
