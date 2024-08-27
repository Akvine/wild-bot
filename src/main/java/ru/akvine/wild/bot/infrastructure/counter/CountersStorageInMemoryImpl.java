package ru.akvine.wild.bot.infrastructure.counter;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.akvine.wild.bot.enums.AdvertStatus;
import ru.akvine.wild.bot.exceptions.ValidationException;
import ru.akvine.wild.bot.constants.ApiErrorConstants;
import ru.akvine.wild.bot.services.AdvertService;
import ru.akvine.wild.bot.services.domain.AdvertModel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Slf4j
public class CountersStorageInMemoryImpl implements CountersStorage {
    private final AdvertService advertService;
    private final Map<Integer, Integer> counters = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        logger.debug("Start init IterationsStorage...");
        List<AdvertModel> runningAdverts = advertService.getAdvertsByStatuses(List.of(AdvertStatus.RUNNING));
        logger.info("Initializing running adverts in size = {}", runningAdverts.size());
        runningAdverts.forEach(advert -> counters.put(advert.getExternalId(), ZERO_COUNT_INIT));
    }

    @Override
    public void add(int advertId) {
        counters.put(advertId, ZERO_COUNT_INIT);
        logger.info("Init counter for advert with id = {}", advertId);
    }

    @Override
    public void increase(int advertId) {
        validateExists(advertId);
        int advertCounter = counters.get(advertId);
        advertCounter += 1;
        counters.replace(advertId, advertCounter);
        logger.info("Increase counter for advert with id = {}, total = {}", advertId, counters.get(advertId));
    }

    @Override
    public void delete(int advertId) {
        validateExists(advertId);
        counters.remove(advertId);
        logger.info("Remove counter for advert with id = {}", advertId);
    }

    @Override
    public boolean check(int advertId, int maxCountBeforeIncrease) {
        validateExists(advertId);
        return counters.get(advertId) % maxCountBeforeIncrease == 0;
    }

    private void validateExists(int advertId) {
        if (!counters.containsKey(advertId)) {
            throw new ValidationException(
                    ApiErrorConstants.GENERAL_ERROR,
                    "Advert with id = [" + advertId + "] not exists!"
            );
        }
    }
}
