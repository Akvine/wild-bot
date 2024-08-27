package ru.akvine.wild.bot.infrastructure.counter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import ru.akvine.wild.bot.entities.infrastructure.IterationCounterEntity;
import ru.akvine.wild.bot.exceptions.IterationCounterNotFoundException;
import ru.akvine.wild.bot.repositories.infrastructure.IterationCounterRepository;

@RequiredArgsConstructor
@Slf4j
public class CountersStorageInDatabaseImpl implements CountersStorage {
    private final IterationCounterRepository iterationCounterRepository;

    @Override
    public void add(int advertId) {
        logger.debug("Init database counter for advert with id = {}", advertId);
        IterationCounterEntity counter = new IterationCounterEntity()
                .setAdvertId(advertId)
                .setCount(ZERO_COUNT_INIT);
        iterationCounterRepository.save(counter);
    }

    @Override
    public void increase(int advertId) {
        logger.info("Increase database counter for advert with id = {}", advertId);
        IterationCounterEntity iterationCounter = get(advertId);
        iterationCounter.increase();
        iterationCounterRepository.save(iterationCounter);
    }

    @Override
    @Transactional
    public void delete(int advertId) {
        logger.info("Delete database counter for advert with id = {}", advertId);
        IterationCounterEntity iterationCounter = get(advertId);
        iterationCounterRepository.delete(iterationCounter);
    }

    @Override
    public boolean check(int advertId, int maxCountBeforeIncrease) {
        IterationCounterEntity iterationCounter = get(advertId);
        return iterationCounter.getCount() % maxCountBeforeIncrease == 0;
    }

    private IterationCounterEntity get(int advertId) {
        return iterationCounterRepository
                .findByAdvertId(advertId)
                .orElseThrow(() -> {
                    String errorMessage = String.format(
                            "Iteration counter with advert id = [%s] not found!",
                            advertId
                    );
                    return new IterationCounterNotFoundException(errorMessage);
                });
    }
}
