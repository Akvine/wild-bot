package ru.akvine.wild.bot.controllers.states;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.QUERY_QR_CODE_BUTTON_TEXT;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.akvine.wild.bot.bot.dto.Payload;
import ru.akvine.wild.bot.bot.dto.Response;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.enums.ClientState;
import ru.akvine.wild.bot.facades.BotViewFacade;
import ru.akvine.wild.bot.facades.QrCodeGenerationServiceFacade;
import ru.akvine.wild.bot.infrastructure.annotations.State;
import ru.akvine.wild.bot.infrastructure.state.StateStorage;
import ru.akvine.wild.bot.services.integration.BotIntegrationAdapter;
import ru.akvine.wild.bot.services.integration.qrcode.QrCodeGenerationService;
import ru.akvine.wild.bot.services.integration.qrcode.QrCodeGenerationServiceType;
import ru.akvine.wild.bot.services.integration.qrcode.dto.GenerateQrCodeRequest;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.services.property.PropertyCodes;
import ru.akvine.wild.bot.services.property.PropertyService;

@State
@Slf4j
public class FillAdvertisingStateResolver extends StateResolver {
    private static final String QR_CODE_FILE_NAME_DEFAULT_NAME = "qr_code.jpg";

    private final BotIntegrationAdapter botIntegrationAdapter;
    private final QrCodeGenerationServiceFacade qrCodeGenerationServiceFacade;

    @Autowired
    public FillAdvertisingStateResolver(
            StateStorage<String, List<ClientState>> stateStorage,
            BotViewFacade botViewFacade,
            TelegramIntegrationService telegramIntegrationService,
            BotIntegrationAdapter botIntegrationAdapter,
            QrCodeGenerationServiceFacade qrCodeGenerationServiceFacade,
            PropertyService propertyService) {
        super(stateStorage, botViewFacade, telegramIntegrationService, propertyService);
        this.botIntegrationAdapter = botIntegrationAdapter;
        this.qrCodeGenerationServiceFacade = qrCodeGenerationServiceFacade;
    }

    @Override
    public Response resolve(Payload payload) {
        super.resolve(payload);
        String chatId = payload.getChatId();
        String text = payload.getMessage().getText();
        BotType botType = payload.getBotType();

        String qrCodeUrl = propertyService.get(PropertyCodes.CustomPropertiesCodes.QR_CODE_URL.getName());
        Response response = new Response(chatId, botType);
        if (text.equals(QUERY_QR_CODE_BUTTON_TEXT)) {
            Map<QrCodeGenerationServiceType, QrCodeGenerationService> serviceMap =
                    qrCodeGenerationServiceFacade.getServicesMap();

            // TODO: подумать над доп. методом в PropertyService для получения группы настроек через префикс по аналогии
            // с @ConfigurationProperties
            String errorCorrectionLevel =
                    propertyService.get(PropertyCodes.QRaftIntegrationPropertiesCodes.ERROR_CORRECTION_LEVEL.getName());
            int qrSize = propertyService.getAs(
                    PropertyCodes.QRaftIntegrationPropertiesCodes.QR_SIZE.getName(), Integer.class);
            int borderSize = propertyService.getAs(
                    PropertyCodes.QRaftIntegrationPropertiesCodes.BORDER_SIZE.getName(), Integer.class);
            int radiusFactor = propertyService.getAs(
                    PropertyCodes.QRaftIntegrationPropertiesCodes.RADIUS_FACTOR.getName(), Integer.class);
            double cornerBlockRadiusFactor = propertyService.getAs(
                    PropertyCodes.QRaftIntegrationPropertiesCodes.CORNER_BLOCK_RADIUS_FACTOR.getName(), Double.class);
            boolean roundInnerCorners = propertyService.getAs(
                    PropertyCodes.QRaftIntegrationPropertiesCodes.ROUND_INNER_CORNERS.getName(), Boolean.class);
            boolean roundOuterCorners = propertyService.getAs(
                    PropertyCodes.QRaftIntegrationPropertiesCodes.ROUND_OUTER_CORNERS.getName(), Boolean.class);
            boolean cornerBlocksAsCircles = propertyService.getAs(
                    PropertyCodes.QRaftIntegrationPropertiesCodes.CORNER_BLOCKS_AS_CIRCLES.getName(), Boolean.class);
            String imageType = propertyService.get(PropertyCodes.QRaftIntegrationPropertiesCodes.IMAGE_TYPE.getName());

            GenerateQrCodeRequest request = new GenerateQrCodeRequest()
                    .setUrl(qrCodeUrl)
                    .setQrSize(qrSize)
                    .setBorderSize(borderSize)
                    .setRadiusFactor(radiusFactor)
                    .setErrorCorrectionLevel(errorCorrectionLevel)
                    .setCornerBlockRadiusFactor(cornerBlockRadiusFactor)
                    .setRoundInnerCorners(roundInnerCorners)
                    .setRoundOuterCorners(roundOuterCorners)
                    .setCornerBlocksAsCircles(cornerBlocksAsCircles)
                    .setImageType(imageType);

            byte[] image;
            boolean qraftIntegrationEnabled = propertyService.getAs(
                    PropertyCodes.QRaftIntegrationPropertiesCodes.INTEGRATION_ENABLED.getName(), Boolean.class);
            if (qraftIntegrationEnabled) {
                try {
                    image = serviceMap.get(QrCodeGenerationServiceType.EXTERNAL).generateQrCode(request);
                } catch (Exception exception) {
                    logger.error(
                            "Some error was occurred while calling external qr code generation service. "
                                    + "Generate message by internal service. Message = [{}]",
                            exception.getMessage());
                    image = serviceMap.get(QrCodeGenerationServiceType.INTERNAL).generateQrCode(request);
                }
            } else {
                image = serviceMap.get(QrCodeGenerationServiceType.INTERNAL).generateQrCode(request);
            }

            botIntegrationAdapter.sendImage(chatId, botType, image, QR_CODE_FILE_NAME_DEFAULT_NAME);
            return response.setText("QR-код для пополнения бюджета сгенерован");
        } else {
            return resolveDefaultResponse(chatId, botType);
        }
    }

    @Override
    public ClientState getState() {
        return ClientState.FILL_ADVERTISING_ACCOUNT_MENU;
    }
}
