package ru.akvine.wild.bot.admin.meta;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.akvine.wild.bot.admin.dto.card.ListCardsRequest;
import ru.akvine.wild.bot.admin.dto.common.Response;

/**
 * Контракт админ-панели для просмотра карточек товара клиентов.
 */
@RequestMapping(value = "/admin/cards")
public interface CardControllerMeta {
    /**
     * Возвращает список карточек товара по заданным условиям фильтрации.
     *
     * @param request параметры фильтрации карточек
     * @return список найденных карточек
     */
    @GetMapping
    Response list(@Valid @RequestBody ListCardsRequest request);
}
