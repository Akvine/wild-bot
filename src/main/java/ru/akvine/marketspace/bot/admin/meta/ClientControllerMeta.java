package ru.akvine.marketspace.bot.admin.meta;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.akvine.marketspace.bot.admin.dto.client.AddToWhitelistRequest;
import ru.akvine.marketspace.bot.admin.dto.client.BlockClientRequest;
import ru.akvine.marketspace.bot.admin.dto.client.SendMessageRequest;
import ru.akvine.marketspace.bot.admin.dto.client.UnblockClientRequest;
import ru.akvine.marketspace.bot.admin.dto.common.Response;
import ru.akvine.marketspace.bot.admin.dto.common.SecretRequest;

@RequestMapping(value = "/admin")
public interface ClientControllerMeta {
    @PostMapping(value = "/client/block")
    Response block(@Valid @RequestBody BlockClientRequest request);

    @PostMapping(value = "/client/unblock")
    Response unblock(@Valid @RequestBody UnblockClientRequest request);

    @PostMapping(value = "/client/block/list")
    Response list(@Valid @RequestBody SecretRequest request);

    @PostMapping(value = "/send/message")
    Response sendMessage(@Valid @RequestBody SendMessageRequest request);

    @PostMapping(value = "/client/add/whitelist")
    Response addToWhiteList(@Valid @RequestBody AddToWhitelistRequest request);
}
