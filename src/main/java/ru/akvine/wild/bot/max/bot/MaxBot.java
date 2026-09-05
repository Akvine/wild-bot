package ru.akvine.wild.bot.max.bot;

import ru.akvine.wild.bot.services.integration.max.dto.Update;
import ru.akvine.wild.bot.services.integration.max.dto.request.SendMessageRequest;

public interface MaxBot {
    SendMessageRequest onUpdateReceived(Update[] updates);
}
