package ru.akvine.marketspace.bot.controller.validatores;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.exceptions.StartAdvertException;
import ru.akvine.marketspace.bot.services.AdvertService;

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
            String errorMessage = String.format(
                    "Превышен лимит по запуску кампаний. Запущено %s/%s",
                    currentRunningAdvertsCount, maxRunningAdvertsLimit);
            String exceptionMessage = String.format(
                    "Limit for max running adverts is reached for chat with id = [%s]",
                    chatId);
            throw new StartAdvertException(errorMessage, exceptionMessage);
        }
    }
}
