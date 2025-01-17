package ru.akvine.wild.bot.admin.meta;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.akvine.wild.bot.admin.dto.client.*;
import ru.akvine.wild.bot.admin.dto.common.Response;

@RequestMapping(value = "/admin/clients")
public interface ClientControllerMeta {
    @GetMapping
    Response list();

    @PostMapping(value = "/add/tests")
    Response addTests(@Valid @RequestBody AddTestsRequest request);

    @PostMapping(value = "/block")
    Response block(@Valid @RequestBody BlockClientRequest request);

    @PostMapping(value = "/unblock")
    Response unblock(@Valid @RequestBody UnblockClientRequest request);

    @PostMapping(value = "/block/list")
    Response listBlocked();

    @PostMapping(value = "/send/message")
    Response sendMessage(@Valid @RequestBody SendMessageRequest request);

    @PostMapping(value = "/whitelist/add")
    Response addToWhitelist(@Valid @RequestBody WhitelistRequest request);

    @PostMapping(value = "/whitelist/delete")
    Response deleteFromWhitelist(@Valid @RequestBody WhitelistRequest request);

    @PostMapping(value = "/send/qr-code")
    Response sendQrCode(@Valid @RequestBody SendQrCodeRequest request);
}
