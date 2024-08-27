package ru.akvine.wild.bot.services.integration.yookassa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class YooKassaIntegrationServiceDummy implements YooKassaIntegrationService {
    @Override
    public boolean tryPayment() {
        logger.info("Successful payment subscription!");
        return true;
    }
}
