package ru.akvine.marketspace.bot.services.integration.yookassa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Profile(value = "local")
public class YooKassaIntegrationServiceDummy implements YooKassaIntegrationService {
    @Override
    public boolean tryPayment() {
        logger.info("Successful payment subscription!");
        return true;
    }
}
