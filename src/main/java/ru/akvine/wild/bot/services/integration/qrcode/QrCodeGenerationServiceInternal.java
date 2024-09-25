package ru.akvine.wild.bot.services.integration.qrcode;

import io.nayuki.qrcodegen.QrCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.services.integration.qrcode.dto.GenerateQrCodeRequest;
import ru.akvine.wild.bot.utils.QrCodeUtils;

@Slf4j
@Service
public class QrCodeGenerationServiceInternal implements QrCodeGenerationService {
    @Override
    public byte[] generateQrCode(GenerateQrCodeRequest request) {
        String url = request.getUrl();
        logger.info("Generate qr code using inner service, url = {}", url);
        QrCode qrCode = QrCode.encodeText(url, QrCode.Ecc.HIGH);
        return QrCodeUtils.convertQrCodeToBytes(qrCode);
    }

    @Override
    public QrCodeGenerationServiceType getType() {
        return QrCodeGenerationServiceType.INTERNAL;
    }
}
