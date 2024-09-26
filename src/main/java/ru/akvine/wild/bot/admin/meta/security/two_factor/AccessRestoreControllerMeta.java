package ru.akvine.wild.bot.admin.meta.security.two_factor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.akvine.wild.bot.admin.dto.common.Response;
import ru.akvine.wild.bot.admin.dto.security.access_restore.AccessRestoreCheckOtpRequest;
import ru.akvine.wild.bot.admin.dto.security.access_restore.AccessRestoreFinishRequest;
import ru.akvine.wild.bot.admin.dto.security.access_restore.AccessRestoreStartRequest;

@RequestMapping(value = "/security/two/factor/access/restore")
public interface AccessRestoreControllerMeta {
    @PostMapping(value = "/start")
    Response start(@Valid @RequestBody AccessRestoreStartRequest request, HttpServletRequest httpServletRequest);

    @PostMapping(value = "/newotp")
    Response newotp(@Valid @RequestBody AccessRestoreStartRequest request, HttpServletRequest httpServletRequest);

    @PostMapping(value = "/check")
    Response check(@Valid @RequestBody AccessRestoreCheckOtpRequest request, HttpServletRequest httpServletRequest);

    @PostMapping(value = "/finish")
    Response finish(@Valid @RequestBody AccessRestoreFinishRequest request, HttpServletRequest httpServletRequest);
}
