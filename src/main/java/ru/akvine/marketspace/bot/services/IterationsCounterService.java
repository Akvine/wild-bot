package ru.akvine.marketspace.bot.services;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.exceptions.ValidationException;
import ru.akvine.marketspace.bot.exceptions.handler.CommonErrorCodes;
import ru.akvine.marketspace.bot.services.domain.AdvertBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class IterationsCounterService {
    private final AdvertService advertService;
    private final Map<String, Integer> counters = new ConcurrentHashMap<>();

    private static final int ONE_COUNT_INIT = 1;

    @PostConstruct
    private void init() {
        logger.debug("Start init IterationsCounterService...");
        List<AdvertBean> runningAdverts = advertService.getAdvertsByStatuses(List.of(AdvertStatus.RUNNING));
        logger.debug("Initializing running adverts in size = {}", runningAdverts.size());
        runningAdverts.forEach(advert -> counters.put(advert.getAdvertId(), ONE_COUNT_INIT));
    }

    public void add(String advertId) {
        counters.put(advertId, ONE_COUNT_INIT);
        logger.debug("Init counter for advert with id = {}", advertId);
    }

    public void increase(String advertId) {
        validateExists(advertId);
        int advertCounter = counters.get(advertId);
        advertCounter += 1;
        counters.replace(advertId, advertCounter);
        logger.debug("Increase counter for advert with id = {}, total = {}", advertId, counters.get(advertId));
    }

    public void delete(String advertId) {
        validateExists(advertId);
        counters.remove(advertId);
        logger.debug("Remove counter for advert with id = {}", advertId);
    }

    public boolean check(String advertId, int maxCountBeforeIncrease) {
        validateExists(advertId);
        return counters.get(advertId) % maxCountBeforeIncrease == 0;
    }

    private void validateExists(String advertId) {
        if (!counters.containsKey(advertId)) {
            throw new ValidationException(
                    CommonErrorCodes.GENERAL_ERROR,
                    "Advert with id = [" + advertId + "] not exists!"
            );
        }
    }
}
