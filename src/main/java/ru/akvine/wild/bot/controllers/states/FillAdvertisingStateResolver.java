package ru.akvine.wild.bot.controllers.states;

import static ru.akvine.wild.bot.constants.telegram.ButtonConstants.QUERY_QR_CODE_BUTTON_TEXT;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@State
@Slf4j
public class FillAdvertisingStateResolver extends StateResolver {
    private static final String QR_CODE_FILE_NAME_DEFAULT_NAME = "qr_code.jpg";

    private final BotIntegrationAdapter botIntegrationAdapter;
    private final QrCodeGenerationServiceFacade qrCodeGenerationServiceFacade;

    @Value("${qr.code.url}")
    private String qrCodeUrl;

    @Value("${qraft.integration.enabled}")
    private boolean qraftIntegrationEnabled;

    @Value("${qraft.request.param.ecl}")
    private String errorCorrectionLevel;

    @Value("${qraft.request.param.qr.size}")
    private int qrSize;

    @Value("${qraft.request.param.border.size}")
    private int borderSize;

    @Value("${qraft.request.param.radiusFactor}")
    private int radiusFactor;

    @Value("${qraft.request.param.cornerBlockRadiusFactor}")
    private double cornerBlockRadiusFactor;

    @Value("${qraft.request.param.roundInnerCorners}")
    private boolean roundInnerCorners;

    @Value("${qraft.request.param.roundOuterCorners}")
    private boolean roundOuterCorners;

    @Value("${qraft.request.param.cornerBlocksAsCircles}")
    private boolean cornerBlocksAsCircles;

    @Value("${qraft.request.param.image.type}")
    private String imageType;

    @Autowired
    public FillAdvertisingStateResolver(
            StateStorage<String, List<ClientState>> stateStorage,
            BotViewFacade botViewFacade,
            TelegramIntegrationService telegramIntegrationService,
            BotIntegrationAdapter botIntegrationAdapter,
            QrCodeGenerationServiceFacade qrCodeGenerationServiceFacade) {
        super(stateStorage, botViewFacade, telegramIntegrationService);
        this.botIntegrationAdapter = botIntegrationAdapter;
        this.qrCodeGenerationServiceFacade = qrCodeGenerationServiceFacade;
    }

    @Override
    public Response resolve(Payload payload) {
        super.resolve(payload);
        String chatId = payload.getChatId();
        String text = payload.getMessage().getText();
        BotType botType = payload.getBotType();

        Response response = new Response(chatId, botType);
        if (text.equals(QUERY_QR_CODE_BUTTON_TEXT)) {
            Map<QrCodeGenerationServiceType, QrCodeGenerationService> serviceMap =
                    qrCodeGenerationServiceFacade.getServicesMap();
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
