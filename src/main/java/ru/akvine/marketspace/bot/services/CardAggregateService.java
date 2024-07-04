package ru.akvine.marketspace.bot.services;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.services.dto.AggregateCard;
import ru.akvine.marketspace.bot.services.domain.CardBean;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardAggregateService {
    public List<AggregateCard> aggregateByCategory(List<CardBean> cards) {
        Preconditions.checkNotNull(cards, "cards is null");
        return cards.stream()
                .collect(Collectors.groupingBy(CardBean::getCategoryId,
                        Collectors.groupingBy(CardBean::getCategoryTitle, Collectors.counting())))
                .entrySet().stream()
                .flatMap(e -> e.getValue().entrySet().stream()
                        .map(innerEntry -> new AggregateCard(innerEntry.getValue().intValue(), e.getKey(), innerEntry.getKey())))
                .collect(Collectors.toList());
    }
}
