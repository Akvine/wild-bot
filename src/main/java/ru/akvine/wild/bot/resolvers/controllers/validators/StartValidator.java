package ru.akvine.wild.bot.resolvers.controllers.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.enums.AdvertStatus;
import ru.akvine.wild.bot.exceptions.AdvertStartLimitException;
import ru.akvine.wild.bot.services.AdvertService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StartValidator {
    private final AdvertService advertService;

    @Value("${max.running.adverts.limit}")
    private int maxRunningAdvertsLimit;

    public void verifyStart(String chatId) {
        int currentRunningAdvertsCount = advertService.getAdvertsByStatuses(List.of(AdvertStatus.RUNNING)).size();
        if (currentRunningAdvertsCount == maxRunningAdvertsLimit) {
            String errorMessage = "Limit for max running adverts is reached!";
            throw new AdvertStartLimitException(errorMessage);
        }
    }
}
