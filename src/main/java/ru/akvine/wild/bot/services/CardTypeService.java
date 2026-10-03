package ru.akvine.wild.bot.services;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.entities.CardTypeEntity;
import ru.akvine.wild.bot.exceptions.CardTypeNotFoundException;
import ru.akvine.wild.bot.infrastructure.cache.CacheNames;
import ru.akvine.wild.bot.repositories.CardTypeRepository;
import ru.akvine.wild.bot.services.domain.CardTypeInfo;

@Service
@RequiredArgsConstructor
public class CardTypeService {
    private final CardTypeRepository cardTypeRepository;

    public CardTypeEntity verifyExistsByType(String type) {
        Preconditions.checkNotNull(type, "type is null");
        return cardTypeRepository
                .findByType(type)
                .orElseThrow(() -> new CardTypeNotFoundException("Card with type = [" + type + "] not found!"));
    }

    /**
     * Тип карточки по названию, Cache-Aside: первый вызов читает БД и кладёт результат в кеш {@link
     * CacheNames#CARD_TYPES}, следующие берут его из кеша. Отсутствующий тип не кешируется (исключение), поэтому
     * новый тип, добавленный синхронизацией, виден сразу.
     */
    @Cacheable(cacheNames = CacheNames.CARD_TYPES, key = "#type")
    public CardTypeInfo getByType(String type) {
        CardTypeEntity entity = verifyExistsByType(type);
        return new CardTypeInfo(entity.getId(), entity.getType());
    }

    /** Сбрасывает кеш типов. Вызывается после изменения справочника синхронизацией с Wildberries */
    @CacheEvict(cacheNames = CacheNames.CARD_TYPES, allEntries = true)
    public void evictCache() {
        // сброс выполняет аспект кеширования
    }
}
