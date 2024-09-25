package ru.akvine.wild.bot.services.integration.trimly;

import ru.akvine.wild.bot.services.integration.trimly.dto.ShortUrlResponse;

public interface TrimlyIntegrationService {
    ShortUrlResponse createTempShortUrl(String originUrl);
}
