package ru.akvine.marketspace.bot.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.akvine.marketspace.bot.admin.converters.ClientConverter;
import ru.akvine.marketspace.bot.admin.dto.client.*;
import ru.akvine.marketspace.bot.admin.dto.common.Response;
import ru.akvine.marketspace.bot.admin.dto.common.SecretRequest;
import ru.akvine.marketspace.bot.admin.dto.common.SuccessfulResponse;
import ru.akvine.marketspace.bot.admin.meta.ClientControllerMeta;
import ru.akvine.marketspace.bot.admin.validator.ClientValidator;
import ru.akvine.marketspace.bot.services.admin.ClientAdminService;
import ru.akvine.marketspace.bot.services.domain.ClientBean;
import ru.akvine.marketspace.bot.services.dto.admin.client.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClientController implements ClientControllerMeta {
    private final ClientAdminService clientAdminService;
    private final ClientValidator clientValidator;
    private final ClientConverter clientConverter;

    @Override
    public Response addTests(@Valid AddTestsRequest request) {
        clientValidator.verifyAddTestsRequest(request);
        AddTests addTests = clientConverter.convertToAddTests(request);
        ClientBean clientBean = clientAdminService.addTestsToClient(addTests);
        return clientConverter.convertToAddTestsResponse(clientBean);
    }

    @Override
    public Response block(@Valid BlockClientRequest request) {
        clientValidator.verifyBlockClientRequest(request);
        BlockClientStart start = clientConverter.convertToBlockClientStart(request);
        BlockClientFinish finish = clientAdminService.blockClient(start);
        return clientConverter.convertToBlockClientResponse(finish);
    }

    @Override
    public Response list(@Valid SecretRequest request) {
        clientValidator.verifySecret(request);
        List<BlockClientEntry> blocked = clientAdminService.listBlocked();
        return clientConverter.convertToListBlockClientResponse(blocked);
    }

    @Override
    public Response unblock(@Valid UnblockClientRequest request) {
        clientValidator.verifyUnblockClientRequest(request);
        UnblockClient unblockClient = clientConverter.convertToUnblockClient(request);
        clientAdminService.unblockClient(unblockClient);
        return new SuccessfulResponse();
    }

    @Override
    public Response sendMessage(@Valid SendMessageRequest request) {
        clientValidator.verifySendMessageRequest(request);
        SendMessage sendMessage = clientConverter.convertToSendMessage(request);
        clientAdminService.sendMessage(sendMessage);
        return new SuccessfulResponse();
    }

    @Override
    public Response addToWhiteList(@Valid WhitelistRequest request) {
        clientValidator.verifyWhitelistRequest(request);
        Whitelist whitelist = clientConverter.convertToWhitelist(request);
        clientAdminService.addToWhiteList(whitelist);
        return new SuccessfulResponse();
    }

    @Override
    public Response deleteFromWhiteList(WhitelistRequest request) {
        clientValidator.verifyWhitelistRequest(request);
        Whitelist whitelist = clientConverter.convertToWhitelist(request);
        clientAdminService.deleteFromWhiteList(whitelist);
        return new SuccessfulResponse();
    }
}
