package ru.akvine.wild.bot.job.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import ru.akvine.wild.bot.services.integration.property.PropertyService;

@RequiredArgsConstructor
@Slf4j
public class SyncPropertiesJob {
    private final PropertyService propertyService;

    @Scheduled(cron = "${sync.application.properties.cron}")
    public void sync() {
        propertyService.sync();
    }
}
