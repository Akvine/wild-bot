package ru.akvine.wild.bot.services.integration.qrcode;

public interface QrCodeGenerationService {
    byte[] generateQrCode(GenerateQrCodeRequest request);

    QrCodeGenerationServiceType getType();
}
