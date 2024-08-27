package ru.akvine.wild.bot.repositories.specifications;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.entities.CardEntity;
import ru.akvine.wild.bot.services.dto.admin.card.ListCards;

@Component
public class CardSpecification {
    public Specification<CardEntity> build(ListCards listCards) {
        Specification<CardEntity> specification = Specification.where(null);

        if (StringUtils.isNotBlank(listCards.getExternalTitle())) {
            specification = specification.and(withExternalTitle(listCards.getExternalTitle()));
        }
        if (listCards.getExternalId() != null) {
            specification = specification.and(withExternalId(listCards.getExternalId()));
        }
        if (StringUtils.isNotBlank(listCards.getCategoryTitle())) {
            specification = specification.and(withCategoryTitle(listCards.getCategoryTitle()));
        }
        if (listCards.getCategoryId() != null) {
            specification = specification.and(withCategoryId(listCards.getCategoryId()));
        }

        return specification;
    }

    private static Specification<CardEntity> withExternalTitle(String externalTitle) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("externalTitle"), externalTitle));
    }

    private static Specification<CardEntity> withExternalId(Integer externalId) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("externalId"), externalId));
    }

    private static Specification<CardEntity> withCategoryTitle(String categoryTitle) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("categoryTitle"), categoryTitle));
    }

    private static Specification<CardEntity> withCategoryId(Integer categoryId) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("categoryId"), categoryId));
    }
}
