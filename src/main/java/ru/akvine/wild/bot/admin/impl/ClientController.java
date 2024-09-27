package ru.akvine.wild.bot.admin.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.akvine.wild.bot.admin.converters.ClientConverter;
import ru.akvine.wild.bot.admin.dto.client.*;
import ru.akvine.wild.bot.admin.dto.common.Response;
import ru.akvine.wild.bot.admin.dto.common.SuccessfulResponse;
import ru.akvine.wild.bot.admin.meta.ClientControllerMeta;
import ru.akvine.wild.bot.admin.validator.ClientValidator;
import ru.akvine.wild.bot.services.admin.ClientAdminService;
import ru.akvine.wild.bot.services.domain.ClientModel;
import ru.akvine.wild.bot.services.dto.admin.GenerateQrCode;
import ru.akvine.wild.bot.services.dto.admin.client.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClientController implements ClientControllerMeta {
    private final ClientAdminService clientAdminService;
    private final ClientValidator clientValidator;
    private final ClientConverter clientConverter;

    @Override
    public Response list() {
        List<ClientModel> clients = clientAdminService.list();
        return clientConverter.convertToClientListResponse(clients);
    }

    @Override
    public Response addTests(@Valid AddTestsRequest request) {
        clientValidator.verifyAddTestsRequest(request);
        AddTests addTests = clientConverter.convertToAddTests(request);
        ClientModel clientBean = clientAdminService.addTestsToClient(addTests);
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
    public Response listBlocked() {
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
    public Response addToWhitelist(@Valid WhitelistRequest request) {
        Whitelist whitelist = clientConverter.convertToWhitelist(request);
        clientAdminService.addToWhitelist(whitelist);
        return new SuccessfulResponse();
    }

    @Override
    public Response deleteFromWhitelist(@Valid WhitelistRequest request) {
        Whitelist whitelist = clientConverter.convertToWhitelist(request);
        clientAdminService.deleteFromWhitelist(whitelist);
        return new SuccessfulResponse();
    }

    @Override
    public Response sendQrCode(@Valid SendQrCodeRequest request) {
        GenerateQrCode generateQrCode = clientConverter.convertToGenerateQrCode(request);
        clientAdminService.sendQrCode(generateQrCode);
        return new SuccessfulResponse();
    }
}
