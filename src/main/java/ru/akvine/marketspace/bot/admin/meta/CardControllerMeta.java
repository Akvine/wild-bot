package ru.akvine.marketspace.bot.admin.meta;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.akvine.marketspace.bot.admin.dto.card.ListCardsRequest;
import ru.akvine.marketspace.bot.admin.dto.common.Response;

@RequestMapping(value = "/admin/cards")
public interface CardControllerMeta {
    @GetMapping
    Response list(@Valid @RequestBody ListCardsRequest request);
}