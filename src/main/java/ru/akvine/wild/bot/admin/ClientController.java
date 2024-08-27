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
import ru.akvine.marketspace.bot.services.domain.ClientModel;
import ru.akvine.marketspace.bot.services.dto.admin.client.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClientController implements ClientControllerMeta {
    private final ClientAdminService clientAdminService;
    private final ClientValidator clientValidator;
    private final ClientConverter clientConverter;

    @Override
    public Response list(@Valid SecretRequest secretRequest) {
        clientValidator.verifySecret(secretRequest);
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
    public Response listBlocked(@Valid SecretRequest request) {
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
    public Response addToWhitelist(@Valid WhitelistRequest request) {
        clientValidator.verifySecret(request);
        Whitelist whitelist = clientConverter.convertToWhitelist(request);
        clientAdminService.addToWhitelist(whitelist);
        return new SuccessfulResponse();
    }

    @Override
    public Response deleteFromWhitelist(@Valid WhitelistRequest request) {
        clientValidator.verifySecret(request);
        Whitelist whitelist = clientConverter.convertToWhitelist(request);
        clientAdminService.deleteFromWhitelist(whitelist);
        return new SuccessfulResponse();
    }

    @Override
    public Response sendQrCode(@Valid SendQrCodeRequest request) {
        clientValidator.verifySecret(request);
        clientAdminService.sendQrCode(
                request.getChatId(),
                request.getText(),
                request.getCaption());
        return new SuccessfulResponse();
    }
}
