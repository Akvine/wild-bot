package ru.akvine.wild.bot.repositories.specifications;

import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.enums.BotType;
import ru.akvine.wild.bot.services.dto.admin.client.ListClients;

@Component
public class ClientSpecification {
    public Specification<ClientEntity> build(ListClients listClients) {
        Specification<ClientEntity> specification = Specification.where(null);

        if (CollectionUtils.isNotEmpty(listClients.getChatIds())) {
            specification = specification.and(withChatIds(listClients.getChatIds()));
        }

        if (CollectionUtils.isNotEmpty(listClients.getClientUuids())) {
            specification = specification.and(withClientUuids(listClients.getClientUuids()));
        }

        if (listClients.getDeleted() != null) {
            specification = specification.and(withDeleted(listClients.getDeleted()));
        }

        if (listClients.getInWhitelist() != null) {
            specification = specification.and(withInWhiteList(listClients.getInWhitelist()));
        }

        if (listClients.getTokenIsNull() != null) {
            if (listClients.getTokenIsNull()) {
                specification = specification.and(withTokenIsNull());
            } else {
                specification = specification.and(withTokenIsNotNull());
            }
        }

        if (listClients.getBotType() != null) {
            specification = specification.and(withBotType(listClients.getBotType()));
        }

        return specification;
    }

    private static Specification<ClientEntity> withChatIds(Set<String> chatIds) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("chatId")).value(chatIds);
    }

    private static Specification<ClientEntity> withClientUuids(Set<String> clientUuids) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.in(root.get("uuid")).value(clientUuids);
    }

    private static Specification<ClientEntity> withDeleted(Boolean deleted) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("deleted"), deleted));
    }

    private static Specification<ClientEntity> withInWhiteList(Boolean inWhiteList) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("inWhitelist"), inWhiteList));
    }

    private static Specification<ClientEntity> withTokenIsNull() {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("token")));
    }

    private static Specification<ClientEntity> withTokenIsNotNull() {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.isNotNull(root.get("token")));
    }

    private static Specification<ClientEntity> withBotType(BotType botType) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("botType"), botType));
    }
}
