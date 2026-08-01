package ru.akvine.wild.bot.job;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import ru.akvine.wild.bot.services.integration.max.MaxIntegrationService;

@RequiredArgsConstructor
public class MaxBotLongPoolingJob {
    private final MaxIntegrationService maxIntegrationService;

    @Scheduled(fixedDelay = 200)
    public void checkUpdates() {
        maxIntegrationService.updates();
    }

}
