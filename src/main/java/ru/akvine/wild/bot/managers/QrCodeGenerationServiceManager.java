package ru.akvine.wild.bot.managers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.services.integration.qrcode.QrCodeGenerationService;
import ru.akvine.wild.bot.services.integration.qrcode.QrCodeGenerationServiceType;

import java.util.Map;

@Getter
@AllArgsConstructor
public class QrCodeGenerationServiceManager {
    private final Map<QrCodeGenerationServiceType, QrCodeGenerationService> servicesMap;
}
