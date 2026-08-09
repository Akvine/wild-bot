package ru.akvine.wild.bot.services;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.services.domain.CardModel;
import ru.akvine.wild.bot.services.dto.AggregateCard;

@Service
@RequiredArgsConstructor
public class CardAggregateService {
    public List<AggregateCard> aggregateByCategory(List<CardModel> cards) {
        Preconditions.checkNotNull(cards, "cards is null");
        return cards.stream()
                .collect(Collectors.groupingBy(
                        CardModel::getCategoryId,
                        Collectors.groupingBy(CardModel::getCategoryTitle, Collectors.counting())))
                .entrySet()
                .stream()
                .flatMap(e -> e.getValue().entrySet().stream()
                        .map(innerEntry ->
                                new AggregateCard(innerEntry.getValue().intValue(), e.getKey(), innerEntry.getKey())))
                .collect(Collectors.toList());
    }
}
