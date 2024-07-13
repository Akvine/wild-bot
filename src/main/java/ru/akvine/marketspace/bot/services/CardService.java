package ru.akvine.marketspace.bot.services;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.entities.CardEntity;
import ru.akvine.marketspace.bot.entities.CardPhotoEntity;
import ru.akvine.marketspace.bot.exceptions.CardNotFoundException;
import ru.akvine.marketspace.bot.repositories.CardPhotoRepository;
import ru.akvine.marketspace.bot.repositories.CardRepository;
import ru.akvine.marketspace.bot.services.domain.CardBean;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.CardDto;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.SizeDto;
import ru.akvine.marketspace.bot.utils.UUIDGenerator;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {
    @Value("${card.uuid.length}")
    private int length;

    private final static int SINGLE_ELEMENT_INDEX = 0;

    private final CardRepository cardRepository;
    private final CardPhotoRepository cardPhotoRepository;

    public List<CardBean> create(List<CardDto> cards) {
        Preconditions.checkNotNull(cards, "cards is null");
        logger.info("Create clients by request with size = {}", cards.size());
        return cards
                .stream()
                .map(cardDto -> {
                    CardEntity cardEntity = new CardEntity()
                            .setUuid(UUIDGenerator.uuidWithoutDashes(length))
                            .setItemId(cardDto.getNmID())
                            .setItemTitle(cardDto.getTitle())
                            .setCategoryId(cardDto.getSubjectID())
                            .setCategoryTitle(cardDto.getSubjectName())
                            .setBarcode(cardDto.getSizes().get(SINGLE_ELEMENT_INDEX).getSkus().get(SINGLE_ELEMENT_INDEX));

                    CardEntity savedCard = cardRepository.save(cardEntity);
                    cardDto.getPhotos().forEach(photo -> {
                        CardPhotoEntity cardPhotoEntity = new CardPhotoEntity()
                                .setBigUrl(photo.getBig())
                                .setCardEntity(savedCard);
                        cardPhotoRepository.save(cardPhotoEntity);
                    });
                    return new CardBean(savedCard);
                })
                .collect(Collectors.toList());
    }

    public List<CardBean> list() {
        logger.info("List clients");
        return cardRepository
                .findAll()
                .stream()
                .map(CardBean::new)
                .collect(Collectors.toList());
    }

    public CardEntity verifyExistsByItemId(String itemId) {
        Preconditions.checkNotNull(itemId, "itemId is null");
        return cardRepository
                .findByItemId(itemId)
                .orElseThrow(() -> new CardNotFoundException("Card with item id = [" + itemId + "] not found!"));
    }
}
