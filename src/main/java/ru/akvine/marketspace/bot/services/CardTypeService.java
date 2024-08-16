package ru.akvine.marketspace.bot.services;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.entities.CardTypeEntity;
import ru.akvine.marketspace.bot.exceptions.CardTypeNotFoundException;
import ru.akvine.marketspace.bot.repositories.CardTypeRepository;

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
}
