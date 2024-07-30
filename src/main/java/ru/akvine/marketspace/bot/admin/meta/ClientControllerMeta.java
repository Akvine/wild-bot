package ru.akvine.marketspace.bot.admin.meta;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.akvine.marketspace.bot.admin.dto.client.*;
import ru.akvine.marketspace.bot.admin.dto.common.Response;
import ru.akvine.marketspace.bot.admin.dto.common.SecretRequest;

@RequestMapping(value = "/admin/clients")
public interface ClientControllerMeta {
    @PostMapping(value = "/add/tests")
    Response addTests(@Valid @RequestBody AddTestsRequest request);

    @PostMapping(value = "/block")
    Response block(@Valid @RequestBody BlockClientRequest request);

    @PostMapping(value = "/unblock")
    Response unblock(@Valid @RequestBody UnblockClientRequest request);

    @PostMapping(value = "/block/list")
    Response list(@Valid @RequestBody SecretRequest request);

    @PostMapping(value = "/send/message")
    Response sendMessage(@Valid @RequestBody SendMessageRequest request);

    @PostMapping(value = "/add/whitelist")
    Response addToWhiteList(@Valid @RequestBody WhitelistRequest request);

    @PostMapping(value = "/delete/whitelist")
    Response deleteFromWhiteList(@Valid @RequestBody WhitelistRequest request);
}
