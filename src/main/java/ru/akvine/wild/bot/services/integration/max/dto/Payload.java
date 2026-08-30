package ru.akvine.wild.bot.services.integration.max.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Payload {
    /**
     * Кнопки клавиатуры, сгруппированные по рядам — внешний массив это ряды, внутренний
     * это кнопки внутри одного ряда (см. схему {@code attachments[].payload.buttons}
     * в MAX Bot API: POST /messages).
     */
    private Button[][] buttons;

    /**
     * Токен для загрузки медиафайлов
     */
    private String token;
}
