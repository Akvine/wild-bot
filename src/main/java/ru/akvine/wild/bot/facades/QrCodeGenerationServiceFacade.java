package ru.akvine.wild.bot.facades;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.akvine.wild.bot.services.integration.qrcode.QrCodeGenerationService;
import ru.akvine.wild.bot.services.integration.qrcode.QrCodeGenerationServiceType;

@Getter
@AllArgsConstructor
public class QrCodeGenerationServiceFacade {
    private final Map<QrCodeGenerationServiceType, QrCodeGenerationService> servicesMap;
}
