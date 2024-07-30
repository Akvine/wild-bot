package ru.akvine.marketspace.bot.services.counter;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.exceptions.ValidationException;
import ru.akvine.marketspace.bot.exceptions.handler.CommonErrorCodes;
import ru.akvine.marketspace.bot.services.AdvertService;
import ru.akvine.marketspace.bot.services.domain.AdvertBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Slf4j
public class IterationsCounterServiceInMemoryImpl implements IterationsCounterService {
    private final AdvertService advertService;
    private final Map<Integer, Integer> counters = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        logger.debug("Start init IterationsCounterService...");
        List<AdvertBean> runningAdverts = advertService.getAdvertsByStatuses(List.of(AdvertStatus.RUNNING));
        logger.info("Initializing running adverts in size = {}", runningAdverts.size());
        runningAdverts.forEach(advert -> counters.put(advert.getAdvertId(), ZERO_COUNT_INIT));
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
                    CommonErrorCodes.GENERAL_ERROR,
                    "Advert with id = [" + advertId + "] not exists!"
            );
        }
    }
}