package ru.akvine.marketspace.bot.services;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.entities.CardEntity;
import ru.akvine.marketspace.bot.entities.CardTypeEntity;
import ru.akvine.marketspace.bot.exceptions.CardNotFoundException;
import ru.akvine.marketspace.bot.exceptions.CardTypeNotFoundException;
import ru.akvine.marketspace.bot.repositories.CardRepository;
import ru.akvine.marketspace.bot.repositories.CardTypeRepository;
import ru.akvine.marketspace.bot.services.domain.CardModel;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.CardCharacteristic;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.CardDto;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.SizeDto;
import ru.akvine.marketspace.bot.utils.UUIDGenerator;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final CardTypeRepository cardTypeRepository;

    public List<CardModel> create(List<CardDto> cards) {
        Preconditions.checkNotNull(cards, "cards is null");
        logger.info("Create cards by request with size = {}", cards.size());

        List<CardTypeEntity> cardTypes = cardTypeRepository.findAll();
        return cards
                .stream()
                .map(cardDto -> {
                    CardEntity cardEntity = new CardEntity()
                            .setUuid(UUIDGenerator.uuidWithoutDashes())
                            .setExternalId(cardDto.getNmID())
                            .setExternalTitle(cardDto.getTitle())
                            .setCategoryId(cardDto.getSubjectID())
                            .setCategoryTitle(cardDto.getSubjectName());

                    if (cardDto.getSizes().size() > 1) {
                        String errorMessage = String.format(
                                "Card with nm ID = [%s] has more 1 sizes = [%s]",
                                cardDto.getNmID(), cardDto.getSizes().size());
                        throw new IllegalStateException(errorMessage);
                    }
                    SizeDto size = cardDto.getSizes().getFirst();
                    if (size.getSkus().size() > 1) {
                        String errorMessage = String.format(
                                "For card with nm ID = [%s] has size that has more 1 barcodes = [%s]",
                                cardDto.getNmID(), size.getSkus().size());
                        throw new IllegalStateException(errorMessage);
                    }
                    cardEntity.setBarcode(size.getSkus().getFirst());
                    cardEntity.setCardType(getCardType(cardDto, cardTypes));

                    CardEntity savedCard = cardRepository.save(cardEntity);
                    return new CardModel(savedCard);
                })
                .collect(Collectors.toList());
    }

    public List<CardModel> getByType(String cardType) {
        logger.info("List cards by cardType = {}", cardType);
        return cardRepository
                .findByCardType(cardType)
                .stream()
                .map(CardModel::new)
                .toList();
    }

    public CardEntity verifyExistsByExternalId(int externalId) {
        return cardRepository
                .findByExternalId(externalId)
                .orElseThrow(() -> new CardNotFoundException("Card with external id = [" + externalId + "] not found!"));
    }

    public CardModel getFirst(int categoryId) {
        logger.info("Get first card by categoryId = {}", categoryId);

        try {
            return getByCategoryId(categoryId).getFirst();
        } catch (NoSuchElementException exception) {
            throw new CardNotFoundException("No one card with category id = [" + categoryId + "] not found!");
        }
    }

    public List<CardModel> getByCategoryId(int categoryId) {
        logger.info("Get cards by category id = {}", categoryId);
        return cardRepository
                .findByCategoryId(categoryId)
                .stream()
                .map(CardModel::new)
                .toList();
    }

    private CardTypeEntity getCardType(CardDto cardDto, List<CardTypeEntity> cardTypes) {
        String cardDtoType = extractCardDtoType(cardDto);
        for (CardTypeEntity cardType : cardTypes) {
            if (cardType.getType().equals(cardDtoType)) {
                return cardType;
            }
        }

        String errorMessage = String.format(
                "For card with external id = [%s] not found type = [%s]",
                cardDto.getNmID(),
                cardDtoType
        );
        throw new CardTypeNotFoundException(errorMessage);
    }

    private String extractCardDtoType(CardDto cardDto) {
        String cardCharacteristicName = "Пол";
        for (CardCharacteristic characteristic : cardDto.getCharacteristics()) {
            if (characteristic.getName().equals(cardCharacteristicName)) {
                return characteristic.getValue().getFirst();
            }
        }

        String errorMessage = String.format(
                "For card with external id = [%s] has no characteristic with type = [%s]",
                cardDto.getNmID(),
                cardCharacteristicName
        );
        throw new CardTypeNotFoundException(errorMessage);
    }
}
