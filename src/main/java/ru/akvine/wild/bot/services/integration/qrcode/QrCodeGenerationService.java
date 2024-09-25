package ru.akvine.wild.bot.services.integration.qrcode;

import ru.akvine.wild.bot.services.integration.qrcode.dto.GenerateQrCodeRequest;

public interface QrCodeGenerationService {
    byte[] generateQrCode(GenerateQrCodeRequest request);

    QrCodeGenerationServiceType getType();
}
